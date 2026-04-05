# Mobile Computing and Its Applications (Spring 2026)
# Assignment 1: Data Collection App

- **Instructor:** Prof. Youngki Lee
- **Deadline:** 11:59 pm, April 13th (Monday), 2026
- **Document date shown in PDF header:** March 31, 2026

---

## 1. Introduction

In this assignment, you will develop a simple data-capturing app for smartphones. In particular, you will collect data from inertial sensors and cameras for data collection. Figure 1 illustrates the main front page of the app, featuring two tasks linked with buttons. Subsequent figures, Figure 2 and Figure 3, depict the screens displayed when each corresponding button is selected by the user. You are required to submit a detailed report documenting your implementation details and the design decisions behind them.

**Figure 1.** Application Preview.  
**Figure 2.** IMU Collection.  
**Figure 3.** Image Collection.

---

## General Instructions

- You can refer to the Internet or use AI for implementation, but you need to code **ALONE**.
- We will use the automated copy detector to check the possible plagiarism. It is likely to mark a pair of codes as a copy if two students discuss the idea without looking at each other’s code. Of course, we will evaluate the similarity of a pair compared to the overall similarity for the entire class.
- If we doubt that the code may be written by someone else or copied, we reserve the right to request an explanation.
- For the first event of plagiarism, you will get 0 marks for the specific assignment. You will fail the course and be reported to the dean if you copy others’ code more than once.
- Homework needs to be submitted electronically on ETL. Please follow the submission guidelines (Section 2).
- **Honor Code:** This document is exclusively for Spring 2026, 4190.406B students with Professor Youngki Lee at Seoul National University. Please request permission if you want to use this assignment material for other purposes.

---

## 2. Submission Guidelines

- Name the project as **Assignment 1**.
- You must submit a document that explains your implementation, and test details, along with your code.
  - Name the document as **`Assignment 1 Report 20XX-XXXXX.pdf`** (your student ID).

> **IMPORTANT:** You will **NOT** receive points for each feature without a detailed explanation of how you implemented it and why you made each design choice in the document. TA will evaluate the assignment based on your document and implemented application.

- The app must be runnable on the TA’s mobile devices. Please clearly state the environment you developed and tested the app (e.g., OS, device version, SDK version, etc.) in the document.
- You are allowed to use **Android with Java or Kotlin** for implementation.
- Compress the whole directory and name the file **`20XX-XXXXX.zip`** (your student ID).
  - You have to submit a **single compressed file**, not individual files.
  - The directory structure should be as shown below.

```text
20XX-XXXXX.zip
└── Assignment 1
    ├── .gradle/...
    ├── .idea/...
    ├── app/...
    ├── gradle/...
    ├── ...
    └── Assignment 1 Report 20XX-XXXXX.pdf
```

- The app must be able to store the labeled data in the appropriate directory. Please state which directory the data is stored in the document. Below is an example of an output directory structure to store the labeled data. Feel free to use any adequate file name to store the data, but be careful not to overwrite the file.

```text
Android/media/com.TA.Assignment_1
└── output/
    ├── Inertial/
    │   ├── Running/
    │   │   ├── accel_100_000001.csv
    │   │   └── ...
    │   ├── Sitting/
    │   │   ├── accel_50_000002.csv
    │   │   └── ...
    │   └── Standing/
    │       ├── gyro_6_000003.csv
    │       └── ...
    └── Camera/
        ├── Bicycle/
        │   ├── 1440p_000001.png
        │   └── ...
        ├── Table/
        │   ├── 1080p_000002.png
        │   └── ...
        └── Bookcase/
            ├── 720p_000003.png
            └── ...
```

---

## 3. Using Inertial Sensors [3 Points]

We learned how to utilize inertial sensor data to monitor activity and gestures. You will collect labeled inertial data from the three sensors:

- accelerometer
- gyroscope
- magnetometer

### 3.1 Features

Figure 2 shows an example implementation. You are required to implement the following three features. These are the must-have parts you need to create for this assignment.

- **Capture and Save [1 Point]**
  - Implement Data Capture & Save Button for 3-Axis Inertial Sensor
  - Document the Details of the Data Format and Location

- **Label [1 Point]**
  - Implement Activity Labeling and Selection Feature
  - Document the Details of the Labeling Feature

- **Table & Graph Visualization [1 Point]**
  - Implement Real-Time Acceleration Visualization Feature with Table & Graph
  - Document the Details of Visualization Feature

### 3.2 Implementation Details

#### 3.2.1 Capture and Save [1 Point]

You need to implement a feature to capture and save three types of 3-axis inertial sensor data as shown in Figure 4. Table 1 provides one example of the recorded data ([1]); however, you are encouraged to conceptualize and formulate your own data structure. Please describe your data structure, file format, and the directory, and justify your design in the document.

**Figure 4.** Capture Buttons in App.

**Table 1. Captured Data from Inertial Sensors**

| index | attitude.roll | attitude.pitch | attitude.yaw | rotationRate.x | rotationRate.y | rotationRate.z | userAcceleration.x | userAcceleration.y | userAcceleration.z |
|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| 0 | -0.401497 | 0.59264 | -0.698268 | 2.011302 | -0.599567 | -1.608907 | 0.059502 | -0.232594 | 0.257655 |
| 1 | -0.427473 | 0.554403 | -0.714076 | 1.569494 | -0.414692 | -1.637867 | 0.096113 | -0.18807 | 0.280803 |
| 2 | -0.446741 | 0.522667 | -0.726114 | 1.087934 | -0.090934 | -1.465389 | 0.00091 | -0.189949 | 0.271738 |

#### 3.2.2 Label [1 Point]

**Figure 5.** Selecting the Label for Inertial Sensors.

Provide a way to label the activity for the data you are about to collect, as shown in Figure 5. The sample app has three kinds of actions demonstrated in Figures 6-8:

- adding new activities
- removing existing ones
- choosing activity labels

While the app provides specific buttons - **Add**, **Delete**, and **Choose-label** - for these actions, you’re not bound to them; feel free to design your own user-friendly approach. However, a feature for selecting labels, like the Choose-label button, is essential. Please include such a feature and explain how and why you created it as such in your document, ensuring the app remains practical and user-oriented.

**Figure 6.** ‘Add’ Button: Creating a New Activity.  
**Figure 7.** ‘Delete’ Button: Removing ‘Sitting’ Class.  
**Figure 8.** ‘Choose-label’ Button: Selecting ‘Sitting’ Label.

#### 3.2.3 Table & Graph Visualization [1 Point]

Provide a feature to visualize acceleration values in real-time through tables and graphs. Feel free to provide visualization features for the gyroscope and magnetometer. Figure 9 shows the table and graph that visualize the collected data.

**Table Specification**

**Objective:** To display real-time 3-axis acceleration data along with its sensing frequency.

- **Details:**
  - (a) Present the 3-axis acceleration data in tabular format.
  - (b) Ensure the table is updated in real-time to reflect the most recent data.
  - (c) The table should clearly indicate the sensing frequency of the acceleration data.

**Graph Specification**

**Objective:** To visualize the acceleration data in real-time.

- **Details:**
  - (a) Plot the accelerometer data on a graph with the timestamp on the x-axis and the acceleration values on the y-axis.
  - (b) Update the graph in real-time to depict the most current acceleration values.
  - (c) Use distinct markers or lines for each axis to differentiate between them.

**Figure 9.** Table and Graph for Inertial Datas.

#### 3.2.4 [Optional] Other features

Feel free to add other features, e.g., controlling frequency. This is not mandatory but thinking about other useful features will be fun.

---

## 4. Using Camera [2 Points]

The camera is another widely used sensor in smartphones and AR/VR devices. You will now try to capture images from a smartphone camera and store them to create an object classification dataset.

### 4.1 Features

Figure 3 shows a screenshot of the image data collection feature. Similar to the acceleration, you will need to provide two features:

- **Capture and Save [1 Point]**
  - Implement Data Capture and Save Button for Image Data Collection
  - Document the Details of the Data Format and Location

- **Label [1 Point]**
  - Develop and Customize Image Data Labeling Feature for Object Classification
  - Document the Details of the Labeling Feature

### 4.2 Implementation Details

#### 4.2.1 Capture and Save [1 Point]

Provide a function to take a picture and store it in an appropriate folder. Figure 10 shows an example interface. You need to implement the functionality for the **Save** button, designed to capture the preview, and provide a clear written explanation of how and why this feature is implemented as such in the document. Clearly indicate how you designate the file name and directory.

**Figure 10.** Capture and Control Buttons in App.

#### 4.2.2 Label [1 Point]

Implement the feature to label the image data for object classification tasks, similar to labeling the inertial sensor data. You are encouraged to tailor the labeling feature to be more suited for the image data collection. Please explain the design choice you make in your document.

#### 4.2.3 [Optional] Other features

Feel free to add other features. The example app, as illustrated in Figure 10, includes two additional features that allow users to toggle between the front and rear cameras and to adjust the camera resolution. Please describe what you made and how to use it in the document.

---

## References

[1] Mohammad Malekzadeh, Richard G. Clegg, Andrea Cavallaro, and Hamed Haddadi. *Mobile sensor data anonymization*. In *Proceedings of the International Conference on Internet of Things Design and Implementation, IoTDI ’19*, pages 49-58, New York, NY, USA, 2019. ACM.
