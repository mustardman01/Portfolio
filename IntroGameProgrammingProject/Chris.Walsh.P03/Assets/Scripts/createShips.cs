using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class createShips : MonoBehaviour {
    /*
     * Author: Chris Walsh
     * Created On: 11/23/18
     * Updated On: 12/12/18
     * Modified By: Chris Walsh
     */

    [Header("Set in Inspector")]
    public GameObject colonialShip; //reference to ship model
    public float spawnOffset = 10;  //length in seconds before ships spawning

    private Rigidbody shipRigidbody;
    private float spawnTime;
    

    // Use this for initialization
    void Start () {
        GameObject thisShip;
        for (int i = 0; i < 5; i++) {
            thisShip = Instantiate(colonialShip) as GameObject;

            Vector3 location = new Vector3(Random.Range(-200, 200), -35, Random.Range(400, 800));
            thisShip.transform.position = location;
            thisShip.transform.Rotate(0, Random.Range(135, 225), 0);

            shipRigidbody = thisShip.GetComponent<Rigidbody>();
            shipRigidbody.isKinematic = true;
            thisShip = null;
        }//end for
        spawnTime = Time.time;
    }//end Start
	
	// Update is called once per frame
	void Update () {
		if(Time.time > spawnTime + spawnOffset){
            GameObject thisShip = Instantiate(colonialShip) as GameObject;

            Vector3 location = new Vector3(Random.Range(-200, 200), -35, Random.Range(400, 800));
            thisShip.transform.position = location;
            thisShip.transform.Rotate(0, Random.Range(135, 225), 0);

            shipRigidbody = thisShip.GetComponent<Rigidbody>();
            shipRigidbody.isKinematic = true;
            thisShip = null;

            spawnTime = Time.time;
            spawnOffset -= .01f;
        }//end if
	}//end Update
}
