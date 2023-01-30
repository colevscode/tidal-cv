# tidal-cv
TidalCycles continuous control voltage utilities


## Setup

Place the contents of `startup-include.scd` in your supercollider `startup.scd` file after loading superdirt. I suggest inserting it at the end of the `s.waitForBoot` block.

Follow the calibration procedure below.

Optional: adjust supercollider's latency. The default is `0.02`. I bump it to `0.05` to give the server a little more time to process events, and ensure the cv synths can correctly output the signal.

## Usage

Note: generally the synths defined by tidal-cv send DC signals with values between `-1` and `1`. What your soundcard does with this depends on how it's configured. I'm using an [ES-9](https://www.expert-sleepers.co.uk/es9.html) which multiplies the signal by 10, generating `-10v` to `+10v`. 

The following cv and gate synths are created by tidal-cv:

* **cv0 - cvN**: Control voltage synths for each of N orbits. Each cv will target channel N * numChannels, for example, if you're using 2 channels, cv0 = channel 0, cv2 = channel 4.
* **cv0_pitch - cvN_pitch**: Alternate way to control the same cv synths above, however notes are interpreted as frequencies (such as "a5 or bs3") and mapped to voltages using a 1v/octave scale. The frequency is converted from exponential to linear, using "a5" (freq 440) as the origin. Then it's scaled to an octave range defined by `~minCV` and `~maxCV`. The formula for converting frequencies is `(log2(freq/440)-minCV)/(maxCV-minCV)`. My oscillator is happy with a range of `-1` to `9` octaves, but you'll have to experimentally determine what range works for your oscillator.
* **cv0_slew - cvN_slew**: Synths that controls the slew (or portamento) value of the relevant cv.
* **gate0 - gateN**: Synths that output a voltage intended to be used as a gate. By default these expect notes in the range `-1` to `1` which should generate `-10v` to `+10v` from your audio adapter.
* **gate0_trig - gateN_trig**: Synths that trigger a +10v pulse for a short hold period defined by `~gateHold`.
* **gate0_slew - gateN_slew**: Slew control for the gate synths.


### Calibration

I suggest generating a cv clock output, use it to gate an oscillator on your modular synth, and simultaneously generate a simple beat pattern from tidal. Then, listening to the two tracks, adjust the `~latencyCorrection` value until they're in sync. Alternately you can patch both to a scope and line them up visually.

Example:

```
d1 $ stack [
  n "1!4" # s "gate1_trig",
  s "hh!4" 
]
```

