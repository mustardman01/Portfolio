using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class ship : MonoBehaviour {
    /*
     * Author: Chris Walsh
     * Created On: 11/23/18
     * Updated On: 12/12/18
     * Modified By: Chris Walsh
     */

    private Text pointsText;
    private Text lifeText;
    private Rigidbody shipRigidbody;
    private int lives = 3;
    private bool lifeTaken = false;

    // Use this for initialization
    void Start () {
        pointsText = GameObject.Find("pointsText").GetComponent<Text>();

        shipRigidbody = this.GetComponent<Rigidbody>();
        shipRigidbody.isKinematic = false;
        //shipRigidbody.velocity = new Vector3(0, 0, Random.Range(-10, -5));

        //Get the velocity of shot relative to cannon and then worldspace
        Rigidbody cannonRigidbody = gameObject.GetComponent("Rigidbody") as Rigidbody;
        Vector3 locVel2 = transform.InverseTransformDirection(cannonRigidbody.velocity);
        locVel2.z = Random.Range(10, 5);
        shipRigidbody.velocity = transform.TransformDirection(locVel2);

        lifeText = GameObject.FindGameObjectWithTag("lifeText").GetComponent<Text>();
        lifeText.text = "Lives: " + playerStats.PlayerLives;
    }//end Start
	
	// Update is called once per frame
	void Update () {
        Vector3 shipPos = gameObject.transform.position;
        if(shipPos.z < 53 && !lifeTaken){
            playerStats.PlayerLives -= 1;
            lifeTaken = true;
            if(playerStats.PlayerLives < 0){
                UnityEngine.SceneManagement.SceneManager.LoadScene(3);
            }//end if
            else{
                lifeText.text = "Lives: " + playerStats.PlayerLives;
            }//end else
        }//end if
        if (shipPos.y < -100){
            Destroy(gameObject);
        }//end if
    }//end Update

    //Do an action when the ship is hit by a cannonball
    void OnTriggerEnter(Collider coll){
        if (coll.CompareTag("Cannonball")){ //if hit by a cannonball
            Destroy(coll.gameObject);       //destroy the cannonball
            lives--;                        //remove a life
            if(lives == 0){
                shipRigidbody.velocity = new Vector3(0, 0, 0);
                shipRigidbody.useGravity = true;    //sink ship when dead
                playerStats.PlayerPoints += 10;
                pointsText.text = "Points: " + playerStats.PlayerPoints;
            }//end if
        }//end if
    }//end onTriggerEnter
}
