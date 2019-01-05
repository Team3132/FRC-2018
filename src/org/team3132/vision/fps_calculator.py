#!/usr/python
"""
Frames per second calculator
Averages the time across the last max_data_points
"""

import math
import time

class FPSCalculator:
    
    def __init__(self, max_data_points):
        self.max_data_points = max_data_points
        self.timestamps = [0] * max_data_points
        self.pos = 0
    
    # Register a new frame.
    def tick(self):
        self.timestamps[self.pos] = time.time()
        self.pos = (self.pos + 1) % self.max_data_points 
        
    # Return the current fps
    def get(self):
        # Divide the number of non-zero values - 1 by the 
        # time between the min and max values
        num = len(filter(lambda x : x > 0, self.timestamps))
        total_time = max(self.timestamps) - min(self.timestamps)
        if total_time < 0.1: return 0
        return num / total_time
        