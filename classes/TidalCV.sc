TidalCV {
  var <>minOct, <>maxOct, <>triggerHold, <>latencyCorrection, <>offsets;

  *new { |dirt, minOct, maxOct, triggerHold, latencyCorrection|
    var instance = super.newCopyArgs(minOct, maxOct, triggerHold, latencyCorrection);
    instance.offsets = Array.fill(dirt.orbits.size, {|i| 0}); 
    instance.load(dirt);
    ^instance;
  }
    
  load { |dirt|
    dirt.orbits.do({ | orbit, i |
      var chans, cvtag, gatetag;
      chans = dirt.numChannels;

      // CV synths

      cvtag=('cv_np'++i).asSymbol;

      Ndef(cvtag, { | freq = 440, slew=0.01, level |
        var sig = if (
          level > 0,
          level,
          LinLin.kr( log2(freq/440), this.minOct, this.maxOct, 0, 1)
        );
        Lag.ar(K2A.ar(sig), lagTime: slew);
      });

      dirt.soundLibrary.addSynth(('cv'++i).asSymbol, (play: {
        var latency = (~latency ? 0) + (this.latencyCorrection ? 0) + this.offsets[i];
        var n = ~n;
        Ndef(cvtag).wakeUp; // make sure the Ndef runs
        thisThread.clock.sched(latency, {
          Ndef(cvtag).set(\level, n);
        });
      }));

      dirt.soundLibrary.addSynth(('cv'++i++'_pitch').asSymbol, (play: {
        var latency = (~latency ? 0) + (this.latencyCorrection ? 0) + this.offsets[i];
        var freq = ~freq;
        Ndef(cvtag).wakeUp; // make sure the Ndef runs
        thisThread.clock.sched(latency, {
          Ndef(cvtag).set(\level, 0, \freq, freq);
        });
      }));

      dirt.soundLibrary.addSynth(('cv'++i++'_slew').asSymbol, (play: {
        var latency = (~latency ? 0) + (this.latencyCorrection ? 0) + this.offsets[i];
        var n = ~n;
        Ndef(cvtag).wakeUp; // make sure the Ndef runs
        thisThread.clock.sched(latency, {
          Ndef(cvtag).set(\slew, n);
        });
      }));

      dirt.soundLibrary.addSynth(('cv'++i++'_on').asSymbol, (play: {
        ('cv'++i++'_on').postln;
        Ndef(cvtag).play(i*chans);
      }));

      dirt.soundLibrary.addSynth(('cv'++i++'_off').asSymbol, (play: {
        ('cv'++i++'_off').postln;
        Ndef(cvtag).stop;
      }));

      // gate synths

      gatetag = ('gate_np' ++ i).asSymbol;

      Ndef(gatetag, { | level = 0, slew=0.01 |
        Lag.ar(K2A.ar(level), lagTime: slew);
      });

      dirt.soundLibrary.addSynth(('gate'++i).asSymbol, (play: {
        var latency = (~latency ? 0) + (this.latencyCorrection ? 0) + this.offsets[i];
        var n = ~n;
        Ndef(gatetag).wakeUp; // make sure the Ndef runs
        thisThread.clock.sched(latency, {
          Ndef(gatetag).set(\level, n);
        });
      }));

      dirt.soundLibrary.addSynth(('gate'++i++'_slew').asSymbol, (play: {
        var latency = (~latency ? 0) + (this.latencyCorrection ? 0)  + this.offsets[i];
        var n = ~n;
        Ndef(gatetag).wakeUp; // make sure the Ndef runs
        thisThread.clock.sched(latency, {
          Ndef(gatetag).set(\slew, n);
        });
      }));

      dirt.soundLibrary.addSynth(('gate'++i++'_trig').asSymbol, (play: {
        var latency = (~latency ? 0) + (this.latencyCorrection ? 0) + this.offsets[i];
        var waitTime = latency + (this.triggerHold ? 0.05);
        Ndef(gatetag).wakeUp; // make sure the Ndef runs
        thisThread.clock.sched(latency, {
          Ndef(gatetag).set(\level, 0.5);
        });
        thisThread.clock.sched(waitTime, {
          Ndef(gatetag).set(\level, -0.01);
        });
      }));

      dirt.soundLibrary.addSynth(('gate'++i++'_on').asSymbol, (play: {
        ('gate'++i++'_on').postln;
        Ndef(gatetag).play(i*chans+1);
      }));

      dirt.soundLibrary.addSynth(('gate'++i++'_off').asSymbol, (play: {
        ('gate'++i++'_off').postln;
        Ndef(gatetag).stop;
      }));

    });
  }
}
