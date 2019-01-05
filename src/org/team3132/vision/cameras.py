'''
Handles talking to cameras and loading images in a background thread
for performance.

Some useful tips for dealing with muliple cameras from this CD thread:
  http://www.chiefdelphi.com/forums/showthread.php?t=147026


***IMPORTANT***
To lower the exposure of the Microsoft HD3000 Lifecam install v4l2-ctl
and run the following in a terminal

sudo apt-get install v4l-utils

v4l2-ctl --set-fmt-video=width=640,height=480,pixelformat=1
v4l2-ctl -d /dev/video0 -c brightness=80 -c contrast=0 -c saturation=200 -c white_balance_temperature_auto=0 -c power_line_frequency=2 -c white_balance_temperature=10000 -c sharpness=0 -c exposure_auto=1 -c exposure_absolute=5 -c pan_absolute=0 -c tilt_absolute=0 -c zoom_absolute=0; v4l2-ctl -d /dev/video1 -c brightness=80 -c contrast=0 -c saturation=200 -c white_balance_temperature_auto=0 -c power_line_frequency=2 -c white_balance_temperature=10000 -c sharpness=0 -c exposure_auto=1 -c exposure_absolute=5 -c pan_absolute=0 -c tilt_absolute=0 -c zoom_absolute=0


Solution was found here:
    https://www.chiefdelphi.com/forums/showthread.php?t=145829

Show how much USB bandwith devices are using:
    cat /sys/kernel/debug/usb/devices | grep "B: "
'''

import cv2
import numpy as np
import time
import sys
import os
from subprocess import check_output
import threading
import traceback
from numpy import rate

ERROR_IMAGE_NAME = "error_image.png"

def getBrokenImage():
    image = cv2.imread(ERROR_IMAGE_NAME, 1)
    return image

# handles grabbing images from a usb camera
class UsbCameraInterface(threading.Thread):
    def __init__(self, id):
        threading.Thread.__init__(self)
        self.daemon = True
        self.capture_timestamp = time.time()
        self.image = getBrokenImage()
        self.broken = False
        self.ret = None
        self.enabled = True
        self.cap = cv2.VideoCapture(id)
        self.condition = threading.Condition()
        self.start()

    def enable(self):
        self.enabled = True

    def disable(self):
        self.enabled = False

    def set_broken(self, state):
        self.broken = state

    def is_broken(self):
        return self.broken

    # Grabs an image from a usb camera and notifies get_image
    def grab_image(self):
        try:
            #print("Trying to grab an image from %s" % self.cap)
            while True:
                capture_timestamp = time.time()
                ret, image = self.cap.read()
                if not ret:
                    print("Failed to get image from camera %s" % self.cap)
                    time.sleep(0.1)
                    # Use the broken image instead.
                    image = getBrokenImage()
                if image is None:
                    print("Got a null image from the camera, trying again")
                    continue
                image = image[0:480, 0:640]
                self.condition.acquire()
                self.capture_timestamp = capture_timestamp
                self.image = image
                self.condition.notify()
                self.condition.release()
        except Exception, e:
            print("Image grab failed: %s" % e)
            traceback.print_exc(file=sys.stdout)
            time.sleep(1)

    # called by the main processing thread to acquire a new image
    def get_image(self, old_timestamp):
        self.condition.acquire()
        if old_timestamp == self.capture_timestamp:
            self.condition.wait()
        self.condition.release()
        return self.image, self.capture_timestamp

    # Get an image from the camera and check to make sure it is returning images
    # this may occur when a camera is not present on the robot or if it is already
    # in use
    def check_camera(self):
        ret, image = self.cap.read()
        if image is None:
            print "*************************************************************"
            print "ERROR: Failed to get a image from a camera, disabling it"
            print "*************************************************************"
            self.set_broken(True)

    def run(self):
        while not self.broken:
            if self.enabled:
                self.grab_image()
            else:
                time.sleep(0.005)

class BouncingValue:
    def __init__(self, min_value, max_value, rate):
        self.min_value = min_value
        self.max_value = max_value
        self.rate = rate
        self.value = (min_value + max_value) / 2
        
    def get(self):
        self.value += self.rate
        if self.value < self.min_value:
            self.value = self.min_value
            self.rate = abs(self.rate)
        elif self.value > self.max_value:
            self.value = self.max_value
            self.rate = -abs(self.rate)
        return self.value
    
class BouncingValuePair:
    def __init__(self, min1, min2, max1, max2, rate1, rate2):
        self.value1 = BouncingValue(min1, max1, rate1)
        self.value2 = BouncingValue(min2, max2, rate2)
        
    def get(self):
        return [self.value1.get(), self.value2.get()]

# handles grabbing images from a local file specified by image_name
class LocalFileCameraInterface(threading.Thread):
    def __init__(self, image_name, distort=True):
        threading.Thread.__init__(self)
        self.broken = False
        self.daemon = True
        self.capture_timestamp = None
        self.image = getBrokenImage()
        self.image_name = image_name
        self.distort = distort
        self.distort_c = BouncingValuePair(40, 40, 480 - 40, 640 - 40, 2, 2)
        # {top|bottom} {left|right}
        self.distort_tl = BouncingValuePair(-40, -40, 40, 40,  1,  1)
        self.distort_tr = BouncingValuePair(-40, -40, 40, 40, -1,  1)
        self.distort_br = BouncingValuePair(-40, -40, 40, 40,  1, -1)
        self.distort_bl = BouncingValuePair(-40, -40, 40, 40, -1, -1)
        self.condition = threading.Condition()
        self.start()

    def enable(self):
        pass

    def disable(self):
        pass

    def set_broken(self, state):
        self.broken = state

    def is_broken(self):
        return self.broken

    # Grabs an image from a usb camera and notifies get_image
    def grab_image(self):
        capture_timestamp = time.time()
        image = cv2.imread(self.image_name, 1)
        if image is None:
            print("Unable to open image file %s" % self.image_name)
            sys.exit(1)
        image = image[0:480, 0:640]
        if self.distort:
            image = self.distort_image(image)
        self.condition.acquire()
        self.capture_timestamp = capture_timestamp
        self.image = image
        self.condition.notify()
        self.condition.release()

    # called by the main processing thread to acquire a new image
    def get_image(self, old_timestamp):
        self.condition.acquire()
        if old_timestamp == self.capture_timestamp:
            self.condition.wait()
        self.condition.release()
        return self.image, self.capture_timestamp

    # get an image from the camera and check to make sure it is returning images
    # this may occur when a camera is not present on the robot or if it is already
    # in use
    def check_camera(self):
        image = cv2.imread(self.image_name, 1)
        if image is None:
            print "*************************************************************"
            print "ERROR: Failed to get a image from local file, giving up"
            print "*************************************************************"
            self.set_broken(True)

    # Moves image around the screen and applies a distortion to it to stretch
    # the image so that it doesn't always appear straight on.
    def distort_image(self, image):
        h,w,dummy = image.shape        
        c = self.distort_c.get()
        tl = self.distort_tl.get()
        tr = self.distort_tr.get()
        bl = self.distort_bl.get()
        br = self.distort_br.get()
        tl = [c[0] + tl[0] - 480/2, c[1] + tl[1] - 640 /2]
        tr = [c[0] + tr[0] - 480/2, c[1] + tr[1] + 640 /2]
        bl = [c[0] + bl[0] + 480/2, c[1] + bl[1] - 640 /2]
        br = [c[0] + bl[0] + 480/2, c[1] + br[1] + 640 /2]
        # Take the full image
        pts1 = np.float32([[0,0],[h,0],[0,w],[h,w]])
        # Stretch around on the destination
        pts2 = np.float32([tl,bl,tr,br])
        M = cv2.getPerspectiveTransform(pts1,pts2)

        #self.t += 1
        return cv2.warpPerspective(image,M,(h,w))

    def run(self):
        while not self.broken:
            time.sleep(0.02)
            self.grab_image()

# Test only code, streams images from the local camera.
if __name__ == "__main__":
    cap = cv2.VideoCapture(id)
    timestamp = 0
    while(True):
        image, timestamp = cap.get_image(timestamp)
        cv2.imshow('Image', image)
        cv2.waitKey(1)
