"""
Handles the user interface if it's been enabled.

To turn on the user interface, pass --use_screen on the command line.
"""

import cv2
import numpy as np
import math
import time
import sys


class ScreenHandler():
    def __init__(self, mask_values, enabled, debug):
        self.enabled = enabled
        self.mask_values = mask_values
        self.debug = debug
        if not enabled: return
        cv2.namedWindow('Trackbars', cv2.WINDOW_NORMAL)
        cv2.resizeWindow('Trackbars', 640, 480)
        if debug:
            cv2.namedWindow('Mask', cv2.WINDOW_NORMAL)
        cv2.namedWindow('Image', cv2.WINDOW_NORMAL)
        cv2.resizeWindow('Image', 640, 480)

        # Create the trackbars
        trackbars = ['H1', 'S1', 'V1', 'H2', 'S2', 'V2']
        for i, name in enumerate(trackbars):
            # Extra i=i to work around python capturing the address of i, not it's value.
            cv2.createTrackbar(name, 'Trackbars', mask_values[i], 255, lambda v,i=i: self.update_mask(i, v))

    # Callback for when a trackbar changes.
    def update_mask(self, index, value):
        self.mask_values[index] = value

    # Gets the positions of trackbars
    def get_mask_values(self):
        return self.mask_values

    def display_image(self, image, image_mask, debug):
        if not self.enabled:
            return
        if (cv2.getWindowProperty('Image', 0) < 0 or
            cv2.getWindowProperty('Trackbars', 0) < 0):
            sys.exit(1)
        cv2.imshow('Image', image)
        if self.debug:
            if cv2.getWindowProperty('Mask', 0) < 0:
                sys.exit(1)
            cv2.imshow('Mask', image_mask)
        key = cv2.waitKey(1)
        if key == 27 or key == ord('q'):
            sys.exit(0)