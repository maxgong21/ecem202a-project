
## TESTING SCIKIT.KINEMATICS https://pypi.org/project/scikit-kinematics/

import numpy as np
import matplotlib.pyplot as plt
from skinematics.sensors.manual import MyOwnSensor


## Calculate position from IMU data
def calculate_position(rate, acc, gyr, mag):
    
    print('... Calculating position ...')
    
    # Calculate position over several time intervals (as opposed to the entire time length of data)
    totalCount = acc.shape[0]
    currentCount = 0
    positions = np.zeros((1,3))
    currentPosition = np.zeros(3)
    currentRotation = np.eye(3)
    
    ###### PROBLEM IS THAT: THE LIBRARY DOES NOT TAKE INTO ACCOUNT PREVIOUS ORIENTATION & POSITION when calculating position ######
    while currentCount < totalCount:
        previousCount = currentCount
        currentCount += 30                       # time interval = 10 data points
        
        if currentCount < totalCount:            # if the current count does not exceed total data count
            in_data = {'rate': rate, 'acc': acc[previousCount:currentCount, :], 'omega': gyr[previousCount:currentCount, :], 'mag': mag[previousCount:currentCount, :]}
        else:
            in_data = {'rate': rate, 'acc': acc[previousCount:totalCount, :], 'omega': gyr[previousCount:totalCount, :], 'mag': mag[previousCount:totalCount, :]}
        # note -- their kalman & analytics filter might be flawed because they convert deg2rad when it's already rad. 
        
        tempSensor = MyOwnSensor(in_file='sensorTest', q_type='madgwick', R_init=currentRotation, calculate_position=True, pos_init = currentPosition, in_data=in_data)  
        positions = np.vstack((positions, tempSensor.pos))       # update positions array   
        currentPosition = positions[positions.shape[0]-1,:]      # update current position with the last calculated position of that time interval
        currentRotation = ScipyRotation(tempSensor.quat[tempSensor.quat.shape[0]-1]).as_matrix()      # get current rotation matrix from most recent quarternion output
        
        print(str(currentCount) + " " + str(currentPosition[0:2]))
        map_trace(tempSensor.pos)
            
    print("... Mapping 1 ...")
    map_trace(positions)
    
    in_data = {'rate': rate, 'acc': acc, 'omega': gyr, 'mag': mag}
    tempSensor = MyOwnSensor(q_type='madgwick', calculate_position=True, in_data=in_data)
    positions = tempSensor.pos
    
    print("... Mapping 2 ...")
    map_trace(positions)
    
    return positions


## Generate map of positions
def map_trace(positions):
    xPos = positions[:,0]          # 1st column = x
    yPos = positions[:,1]          # 2nd column = y
    plt.scatter(xPos, yPos)
    plt.xlabel('x position')
    plt.ylabel('y position')
    plt.show()