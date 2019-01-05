"""
Takes a an image and returns if there was a goal and details about it.

This needs to be updated each year for the new vision target.
"""
import cv2
import numpy as np
import math
import time

from image_util import *

GOAL_AREA_THRESHOLD = 200
GOAL_FULLNESS_THRESHOLD_LOWER = .6
GOAL_FULLNESS_THRESHOLD_UPPER = 3
GOAL_ASPECT_THRESHOLD_LOWER = 2
GOAL_ASPECT_THRESHOLD_UPPER = 14
GOAL_TARGET_ASPECT_THRESHOLD_LOWER = 1
GOAL_TARGET_ASPECT_THRESHOLD_UPPER = 3
GOAL_TARGET_DISTANCE_FACTOR = 12000 # Needs tuning

# Finds 2017 vision targets in masked image
# This is what needs to be changed every year as the vision target changes.
class GoalDetector():
    def process(self, masked_image, verbose):
        result = Result()

        # find any contours (edges between the white and black on the masked image)
        for contour in find_contours(masked_image):
            # If the contour is too small then ignore it
            area = cal_contour_area(contour)
            if area < GOAL_AREA_THRESHOLD:
            	if verbose:
                    print("Contour too small %.1f" % area)
                continue

            # If there isn't a significant portion of the area of the contour's corners then it cant be a U shape
            corners = cal_corners(contour) # narrows down a contour to a the coordinates of four corners
            corner_area = cal_corner_area(corners)
            
            # make sure corner_area is not 0 to avoid a division by zero error
            if (corner_area < 1):
                if verbose:
                    print("corner_area == 0")
                continue    

            fullness = area/corner_area
            if fullness < GOAL_FULLNESS_THRESHOLD_LOWER or fullness > GOAL_FULLNESS_THRESHOLD_UPPER:
                if verbose:
                    print("bad fullness %.1f" % fullness)
                continue

            # If it has a completely incorrect aspect ratio then ignore it
            avg_width, avg_height = cal_avg_height_width(corners)
            aspect_ratio = cal_aspect_ratio(avg_width, avg_height)
            if aspect_ratio < GOAL_ASPECT_THRESHOLD_LOWER or aspect_ratio > GOAL_ASPECT_THRESHOLD_UPPER:
                if verbose:
                    print("bad aspect ratio %.1f" % aspect_ratio)
                continue


            # Save the largest vision target found so far
            if area < result.area:
                continue

            result.found = True
            result.contour = contour
            result.area = area
            result.corners = corners
            result.corner_area = corner_area
            result.avg_width = avg_width
            result.avg_height = avg_height
            result.aspect_ratio = aspect_ratio

        result.image_size = cal_image_size(masked_image)
        if result.found:
            # A vision target was found. Do some more calculations.
            result.goal_center = cal_goal_center(result.corners)
            result.distance = cal_distance(result.avg_width, GOAL_TARGET_DISTANCE_FACTOR)
            result.aim = cal_aim(result.image_size, result.goal_center)

        return result
