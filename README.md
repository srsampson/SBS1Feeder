#### VRSBS1Feeder - An ADS-B Feeder Using the Kinetic SBS-1 Receiver
This was programmed back in 2012, and I no longer have this device.

It is an example of the convolutions a company will go through to protect their data. Originally the data was delayed, but later the data was just sent in real-time, and the purpose of scrambling the data became obsolete.

The data scrambling using two CRC generators is novel.

This used the Java USB Driver from the Sourceforge project: https://sourceforge.net/projects/d2xx/

Uploaded for historical reference.

The program was designed to connect to the SBS-1, drink in the data, unscramble it, and then feed it to a Multicast port in an ASCII format. Since the SBS-1 did the ICAO parity check in firmware, the parity is put back, so the decoders can do it again manually. The ASCII format was useful as it had data delimiters, and could be easily parsed.
