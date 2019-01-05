#!/usr/bin/python
#
# Vision target detection script.
#
# Before running, install OpenCV by running:
#  sudo apt-get install python-opencv
#
# For testing without a camera:
#  ./main.py -s -d -o /tmp/output -i boiler.png
#
# For testing with a camera, run:
#  ./main.py -s -d -o /tmp/output
#
# When run on the robot, run:
#  ./main.py -r -o /var/www/html/vision/images


import cv2
import os
from fps_calculator import FPSCalculator
from goal_detector_2017 import GoalDetector
from screen_handler import *
from control_connection import *
from cameras import *
from image_saver import *
from image_util import *
from live_image_server import LiveImageServer
import time
import optparse
import sys

class Options:
    def __init__(self):
        parser = optparse.OptionParser()
        parser.add_option("-s", "--use_screen", action = "store_true", dest = "use_screen", default = False, help = "run with a screens") 
        parser.add_option("-r", "--con_to_robot", action = "store_true", dest = "con_to_robot", default = False, help = "connect to the robot") 
        parser.add_option("-i", "--input_file", dest = "input_file", default = None, help = "use a local image file instead") 
        parser.add_option("-o", "--output_dir", dest = "output_dir", default = None, help = "directory to write images to") 
        parser.add_option("-d", "--debug", action = "store_true", dest = "debug", default = False, help = "show mask, all goals found") 
        parser.add_option("-v", "--verbose", action = "store_true", dest = "verbose", default = False, help = "be verbose") 
        parser.add_option("-n", "--hostname", dest = "hostname", default = "roborio-3132-frc.local", help = "host to connect to to send results") 
        parser.add_option("-p", "--port", dest = "port", default = 5801, help = "port to connect to to send results")
        parser.add_option("-e", "--viewer_port", dest = "viewer_port", default = 5802, help = "local port to run live image server on") 

        (options, args) = parser.parse_args()
        self.use_screen = options.use_screen
        self.con_to_robot = options.con_to_robot
        self.input_file = options.input_file
        self.output_dir = options.output_dir
        self.debug = options.debug
        self.verbose = options.verbose
        self.hostname = options.hostname
        self.port = options.port
        self.viewer_port = int(options.viewer_port)
        print options


def main():
    mask_values = [32,100,66,115,255,255]
    #mask_values = [32,209,66,115,255,255]
    options = Options()  # Read from the command line.
    detector = GoalDetector()
    live_image_server = LiveImageServer(options.viewer_port)
    screen = ScreenHandler(mask_values, options.use_screen, options.debug)
    image_saver = ImageSaver(options.output_dir)
    fps = FPSCalculator(10)
    
    camera = None
    capture_timestamp = 0

    # create camera from either a local image or from a real one.
    if options.input_file:
        camera = LocalFileCameraInterface(options.input_file)
    else:
        camera = UsbCameraInterface(0)

    # Connect to the RoboRio to send the results to.
    connection = ControlConnection(options.hostname, options.port, options.con_to_robot)

    time.sleep(2) # wait a moment to allow the camera to initialize / colour balance.
    camera.check_camera() # These set flags as to if the camera is returning images or should be given up on

    print "\n________Finished setting up________\n"

    while True:      
        fps.tick()
        # Get a new image if one is ready.
        image, capture_timestamp = camera.get_image(capture_timestamp)
        if image is None:
            print "null image, continuing"
            time.sleep(1)
            continue

        masked_image = mask_image(image, screen.get_mask_values())
        result = detector.process(masked_image, options.verbose)

        if result.found:
            # Draw the centers of all goals so we can see which ones were
            # found after the match, even on the saved images
            draw_goal_center(image, result.goal_center) 
            
        # Save to disk for after-match debugging.
        image_saver.give_image(image, capture_timestamp)

        annotate_image(image, fps.get(), result, live_image_server.is_image_wanted() or options.debug)

        screen.display_image(image, masked_image, options.debug)
        connection.send(result.toRioString(capture_timestamp))
        
        # Reduce the images resolution and convert it to grayscale due to the
        # low bandwidth over the FMS
        if live_image_server.is_image_wanted():
            image = resize_gray_image(image, 0.4)
            live_image_server.set_image(image)

        sys.stdout.flush()

if __name__ == "__main__":
    main()
