using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEditor.VFX;
using UnityEngine.Experimental.VFX;
using UnityEngine.VFX;

public class Equalizer3 : MonoBehaviour
{
    
    public AudioData_AmplitudeBand adab;
    public VisualEffect vfx;


    static float[] freqs;
    Vector3 bass;
    Vector3 midUps;
    Vector3 added;
    
    public AudioSource music;

    public AudioClip[] songs2try;

    private void Start()
    {
        //Music
        adab = GetComponent<AudioData_AmplitudeBand>();
        vfx = this.GetComponent<VisualEffect>();
        freqs = AudioData_AmplitudeBand._freqBand;

        music = GetComponent<AudioSource>();
        
    }

    void Update()
    {

        vfx.SetFloat("VFXIntensity", adab.Amplitude * 200000);

        vfx.SetFloat("VFXRadius", adab.Amplitude * 300);

        bass.Set(0f, freqs[6] * -2, 0f);
        midUps.Set(0f, freqs[7] * 4, 0f);

        added = bass + midUps;
        vfx.SetVector3("VFXGravity", added);
        
    }
}
