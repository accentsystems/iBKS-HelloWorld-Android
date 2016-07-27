<span id="_Toc456607170" class="anchor"><span id="_Toc456859050" class="anchor"></span></span>iBKS Hello World for Android
==================================================================================================================

**ABSTRACT**

The App demo **iBKS Hello World** is a project that contains the most important functions to begin interacting with a Beacon. 

**AUDIENCE**

This document is focused for App developers who has no experience in beacon communication management

<span id="_Toc456607170" class="anchor"><span id="_Toc456859050" class="anchor"></span></span>Before you start
==================================================================================================================

All you need to start playing with “iBKS Hello World”:

-	Android Studio
-	Android device with 5.0 version or above
-	At least one iBKS Beacon
-	Download iBKS Hello World project
-	Check folder docs for further information


<span id="_Toc456607170" class="anchor"><span id="_Toc456859050" class="anchor"></span></span>Project iBKS Hello World
==============================================================================================================

After downloading de “iBKS Hello World” project, you only have to open it on Android studio and compile it. All the needed libraries are included as dependencies and downloaded automatically.

The project is structured to show three important functionalities, each one on a different class:

- **ScanActivity**: scans and list the beacons that are advertising around and allows discovering services and characteristics.
- **NotificationDemo**: Show a notification dialog on App triggered by a specific beacon packet detected.
- **BackgroundScan**: Starts background scan that allows to detect beacons and does some actions (send notification, open app, ...) even when the app is stopped.

The first activity started on foreground is **MainActivity** that show the different options of the app and check the app permissions.

========================================================================================================
Last update: 2016/07/27
