using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class shipComponent : MonoBehaviour {
    /*
     * Author: Chris Walsh
     * Created On: 12/4/18
     * Updated On: 12/11/18
     * Modified By: Chris Walsh
     */

	// Use this for initialization
	void Start () {
		
	}//end start
	
	// Update is called once per frame
	void Update () {
		
	}//end update

    //Destroy this component of the ship when the cannonball hits
    void OnTriggerEnter(Collider coll){
        if (coll.CompareTag("Cannonball")){ //if hit by a cannonball
            Destroy(coll.gameObject);   //destroy both cannonball and this object
            Destroy(gameObject);
        }
    }//end ontriggerenter
}
