# tidal-cv
TidalCycles continuous control voltage utilities


## Setup

First evaluate this line in the supercollider environment to install tidal-cv:

```
Quarks.install("https://github.com/colevscode/tidal-cv.git");
```

Then add this line to your supercollider `startup.scd` file after loading superdirt.

```
~tidalcv = TidalCV(~dirt, -1, 9, 0.05, 0.025);
```

TidalCV is initialized with the dirt instance `~dirt` followed by values for `minOct`, `maxOct`, `triggerHold` and `latencyCorrection`. See the section on [the TidalCV class](#the-tidalcv-class) for more information.

You may need to adjust the above parameters. You can do so by following the [calibration](#calibration) procedure below.

Optional: adjust supercollider's latency. The default is `0.02`. I bump it to `0.05` to give the server a little more time to process events, and ensure the cv synths can correctly output the signal. Example: `s.latency = 0.05`.

## Usage

Once you've initialized tidal-cv, you will have access to several new synths in the tidalcycles environment.

*Note: generally the synths defined by tidal-cv send DC signals with values between `-1` and `1`. What your audio adapter does with this depends on how it's configured. I'm using an [ES-9](https://www.expert-sleepers.co.uk/es9.html) which multiplies the signal by 10, generating `-10v` to `+10v`.*

The following cv and gate synths are exported to tidal by tidal-cv:

* **cv0 - cvN**: Control voltage synths for each of N orbits. Each cv will target channel `N * numChannels`, for example, if you're using 2 channels, cv0 = channel 0, cv2 = channel 4.
* **cv0_pitch - cvN_pitch**: Alternate way to control the same cv synths above, however notes are interpreted as frequencies (such as "a5 or bs3") and mapped to voltages using a 1v/octave scale. The frequency is converted from exponential to linear, using "a5" (freq 440) as the origin. Then it's scaled to an octave range defined by `minOct` and `maxOct`. The formula for converting frequencies is `(log2(freq/440)-minOct)/(maxOct-minOct)`. My oscillator is happy with a range of `-1` to `9` octaves, but you'll have to experimentally determine what range works for your oscillator.
* **cv0_slew - cvN_slew**: Synths that controls the slew (or portamento) value of the relevant cv.
* **gate0 - gateN**: Synths that output a voltage intended to be used as a gate. Each gate will target channel `N * numChannels + 1`, for example, if you're using 2 channels, gate0 = channel 1, gate2 = channel 5. By default these expect notes in the range `-1` to `1` which should generate `-10v` to `+10v` from your audio adapter.
* **gate0_trig - gateN_trig**: Synths that trigger a +10v pulse for a short hold period defined by `triggerHold`.
* **gate0_slew - gateN_slew**: Slew control for the gate synths.

For an example use of these synths within tidal, see `tidal-example.hs`.

### Usage Example

Here's a short example of how to use tidal-cv in tidalcycles.

```
-- turn on cv1 and gate1
once $ stack [s "cv1_on", s "gate1_on"]

-- simple baseline
d1 $ stack [
  n "a3 b3 bs3 a4 " # s "cv1_pitch", 
  n "1!4" # s "gate1_trig"
]
```

For a more indepth example see [tidal-example.hs](tidal-example.hs).


## The TidalCV class

This quark creates a single `TidalCV` class with a constructor that initializes and exports a set of synths to tidalcycles. (See [the section on usage](#usage) above)

The following variables are passed to the `TidalCV` constructor, and can later be accessed via the returned instance. 

* **minOct**: A value that represents the minimum frequency in octaves below middle A. Used to calibrate 1v/octave.
* **maxOct**: A value that represents max frequency in octaves above middle A.
* **triggerHold**: Time in seconds to hold the gate high during a gate trigger event. 
* **latencyCorrection**: An adjustment to the delay time when sending cv messages. Units are seconds. See calibration procedure below.

For example, to initialize a `TidalCV` instance, add the following line to your supercollider `startup.scd` file:

```
~tidalcv = TidalCV(~dirt, minOct: -1, maxOct: 9, triggerHold: 0.05, latencyCorrection: -0.025);
```

Later you can adjust the above variables using the tidal-cv instance:

```
~tidalcv.latencyCorrection = 0.0
```


## Calibration

### Calibrating the CV pitch synths

It may be necessary to calibrate the 1v/oct range generated by the cv pitch synths (eg, `cv1_pitch`).

To do this, patch an oscillator module into one of your audio adapter's CV outs. Then generate a sequence of pitches from tidal from both a CV synth, and an audible synth:

```
d1 $ stack [
  n "a3 a5 a7 a9" # s "cv1_pitch",
  n "1" # s "gate1"
]

d2 $ n "a3 a5 a7 a9" # s "arpy" # channel 0

```

This will generate a sequence of 1v/oct levels from channel 2 and a constant high gate from channel 3. You'll also hear the sequence of tones from tidal on channel 0.

By listening to these simultaneously you can fine-tune your modular synth osciallator's 1v/oct knob until the two sounds match up. If you find that your modular synth is playing an octave high or low, you can adjust the octave range using the `~tidalcv` instance in supercollider:

```
~tidalcv.minOct = -2
~tidalcv.maxOct = 8
```

Once you've figured out a range that works, update your `startup.scd` file to use the new values when instantiating the `TidalCV` instance.

```
// your new values
~tidalcv = TidalCV(~dirt, -2, 8, 0.05, 0.025);
```

### Calibrating `latencyCorrection`

It may be necessary to calibrate the delay time used to send messages to the cv synths. I suggest generating a cv clock output, use it to gate an oscillator on your modular synth, and simultaneously generate a simple beat pattern from tidal. Then, listening to the two tracks, adjust the `latencyCorrection` value until they're in sync. Alternately you can patch both to a scope and line them up visually.

Example:

```
d1 $ stack [
  n "1!4" # s "gate1_trig",
  s "hh!4" 
]
```

