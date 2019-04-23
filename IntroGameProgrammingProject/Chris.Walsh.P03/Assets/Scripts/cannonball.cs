using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class cannonball : MonoBehaviour {
    /*
     * Author: Chris Walsh
     * Created On: 12/3/18
     * Updated On: 12/11/18
     * Modified By: Chris Walsh
     */

    [Header("Set in Inspector")]
    public GameObject thisCannonball;   //Reference to the cannonball prefab

	// Use this for initialization
	void Start () {
        
    }//end start
	
	// Update is called once per frame
	void Update () {
        //check if location is below -60, if it is destroy the object
        Vector3 location = thisCannonball.transform.position;
        if(location.y < -60){
            Destroy(thisCannonball);
        }
    }//end update
}
