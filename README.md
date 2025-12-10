This project introduces a smart accident-alert system that uses Vehicle-to-Infrastructure (V2I) communication to enhance road safety and reduce secondary collisions.
Each participating vehicle is equipped with an onboard accident-detection module that continuously monitors driving conditions using data from existing sensors such as accelerometers, gyroscopes, speedometers, and airbag deployment signals. When the system detects a collision or a dangerously rapid deceleration, it immediately triggers an emergency broadcast message. 
This message is transmitted through a V2I communication protocol—such as LoRa, DSRC, Wi-Fi Direct, or 5G V2X—to the nearest streetlight within range.

Upon receiving the authenticated accident signal, the streetlight autonomously switches its illumination to red, acting as an immediate visual hazard marker. 
This alerts approaching drivers in real time, effectively increasing reaction time and reducing the probability of secondary accidents. 
Additionally, the streetlight can propagate the alert to neighbouring lights, forming a “warning corridor” along the road and guiding drivers to slow down well before reaching the accident zone.

The system can be enhanced further through a connected backend network. 
Each alerted streetlight sends accident data—including location coordinates, time of detection, and severity metadata—to a centralized traffic management server. 
Authorities can then dispatch emergency services faster, trigger dynamic traffic rerouting, or integrate the information into smart city dashboards for monitoring and analysis.

The project’s major strengths lie in its minimal infrastructure requirements, real-time responsiveness, and high integration potential with modern intelligent transportation systems. 
By shifting accident detection responsibilities to the vehicle, streetlights remain simple, low-maintenance devices that react to certified broadcasts. 
This design enables widespread adoption, especially in developing cities, without the need for costly sensor installations or complex upgrades.

Overall, this system provides a powerful, proactive approach to roadway safety—leveraging V2I technology to reduce secondary accidents, support emergency response, and build safer urban mobility ecosystems.
