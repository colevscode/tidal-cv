-- turn on cv1, this starts a continuous DC output on channel 2
once $ s "cv1_on"

-- turn on gates (channels 3 and 7)
once $ stack [s "gate1_on", s "gate3_on"]

-- simplest example: output some control voltages
-- depending on your sound card, this may output a pattern of 0, +3v, +6v, +9v
d1 $ n "0 0.3 0.6 0.9" # s "cv1"

-- output pitches to drive oscilators with 1v/oct input
-- you can calibrate this by adjusting the ~minCV and ~maxCV properties
d1 $ n "a3 b3 bs3 a4" # s "cv1_pitch"

-- output a separate gate pattern to channel 3
d2 $ n "[1 0]!4" # s "gate1"

-- set the slew or portamento of cv1
once $ n "0.5" # s "cv1_slew"

d2 $ silence

-- combine them together
d1 $ stack [
  n "a3 b3 bs3 a4 " # s "cv1_pitch", 
  n "0 0.5" # s "cv1_slew", 
  n "[1 0]!4" # s "gate1"
]

-- send a clock signal to channel 7
-- this outputs a sequence of triggers (short pulses of +10v)
d3 $ n "1!4" # s "gate3_trig"
