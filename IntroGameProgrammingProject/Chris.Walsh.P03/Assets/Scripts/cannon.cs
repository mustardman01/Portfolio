using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using TMPro;

public class cannon : MonoBehaviour {
    /*
     * Author: Chris Walsh
     * Created On: 12/4/18
     * Updated On: 12/13/18
     * Modified By: Chris Walsh
     */

    [Header("Set in Inspector")]
    public GameObject playerCannon; //reference to the player(cannon)
    public GameObject cannonBall;   //reference to the cannonBall prefab
    public float power = 20.0f;     //power of shot, controlled by user
    public float shotDelay = 1.0f;  //time between shots in seconds
    public float maxPower = 50.0f;  //max power of cannon

    private TextMeshProUGUI powerText;
    private GameObject projectile;   //projectile object
    private Rigidbody projectileRigidbody;
    private Quaternion cannonRotation = new Quaternion(0, 0, 0, 0);
    private GameObject mainCamera;
    private float shotStart;
    private LineRenderer lR;
    private int numSections = 50;
    private Vector3 startPoint;
    private Vector3 endPoint;
    private Vector3 midPoint;
    private bool activateTracker = false;

    // Use this for initialization
    void Start () {
        powerText = GameObject.Find("powerText").GetComponent<TextMeshProUGUI>();
        mainCamera = GameObject.Find("Main Camera");

        lR = gameObject.GetComponent(typeof(LineRenderer)) as LineRenderer;
        lR.positionCount = numSections;
        lR.material.color = Color.gray;

        startPoint = new Vector3(0, -1, 0); //start of curve
        endPoint = new Vector3(0, 0, 100);  //end of curve
        midPoint = new Vector3(0, 50, 50);  //mid of curve
    }//end Start
	
	// Update is called once per frame
	void Update () {
        if (activateTracker){
            //Get the velocity of shot relative to cannon and then worldspace
            Rigidbody cannonRigidbody = gameObject.GetComponent("Rigidbody") as Rigidbody;
            Vector3 locVel2 = transform.InverseTransformDirection(cannonRigidbody.velocity);
            locVel2.x = power;
            locVel2 = transform.TransformDirection(locVel2);
            float hangTime = 10;

            //end point will be distance traveled
            endPoint = startPoint + locVel2 * hangTime + Physics.gravity * hangTime * hangTime * 0.5f;
            //midpoint is half that distance
            midPoint = startPoint + locVel2 * (hangTime / 2);

            //plot the curve of the shot
            PlotCurve();
        }

        //Get input for movement, rotation, shooting, and power of cannon.
        if (Input.GetKey("d") && playerCannon.transform.rotation.eulerAngles.y < 340)
        {
            print(playerCannon.transform.rotation.eulerAngles.y);
            playerCannon.transform.Rotate(0, 1, 0);
            cannonRotation.y += 1;
            mainCamera.transform.Rotate(0, .5f, 0);
        }
        else if (Input.GetKey("a") && playerCannon.transform.rotation.eulerAngles.y > 200)
        {
            print(playerCannon.transform.rotation.eulerAngles.y);
            playerCannon.transform.Rotate(0, -1, 0);
            cannonRotation.y -= 1;
            mainCamera.transform.Rotate(0, -.5f, 0);
        }
        else if (Input.GetKey("w"))
        {
            playerCannon.transform.Rotate(0, 0, 1);
            cannonRotation.z += 1;
        }
        else if (Input.GetKey("s"))
        {
            playerCannon.transform.Rotate(0, 0, -1);
            cannonRotation.z -= 1;
        }
        else if (Input.GetKey("e"))
        {
            playerCannon.transform.Rotate(-1, 0, 0);
            cannonRotation.x -= 1;
        }
        else if (Input.GetKey("q"))
        {
            playerCannon.transform.Rotate(1, 0, 0);
            cannonRotation.x += 1;
        }
        else if (Input.GetKey(KeyCode.UpArrow))
        {
            if(power < maxPower)
                power += .5f;

            powerText.SetText("Power: " + power);
        }
        else if (Input.GetKey(KeyCode.DownArrow))
        {
            if(power > 10)
                power -= .5f;
            powerText.SetText("Power: " + power);
        }
        //************

        //Shoot when the space key is pressed launch a projectile if it has been long enough
        if (Input.GetKeyDown("space") && Time.time > shotStart+shotDelay)
        {
            shotStart = Time.time;
            projectile = Instantiate(cannonBall) as GameObject;

            Vector3 location = playerCannon.transform.position;
            location.y += .5f;
            location.z += 2;
            projectile.transform.position = location;
            projectileRigidbody = projectile.GetComponent<Rigidbody>();
            projectileRigidbody.isKinematic = false;

            var locVel = transform.InverseTransformDirection(projectileRigidbody.velocity);
            locVel.x = power;
            projectileRigidbody.velocity = transform.TransformDirection(locVel);
            projectile = null;
        }//end if

        //Activate or deactivate the shot tracker when left shift is pressed
        if (Input.GetKeyDown(KeyCode.LeftShift)){
            activateTracker = !activateTracker;

            for (int i = 0; i < numSections; i++){
                lR.SetPosition(i, new Vector3(0, 0, 0));
            }
        }

        //Press escape to exit at any time
        if (Input.GetKeyDown(KeyCode.Escape)){
            UnityEngine.SceneManagement.SceneManager.LoadScene(0);
        }
    }//end Update

    /********* Equations from: https://en.wikipedia.org/wiki/Projectile_motion ***********/
    //Plot each point of the curve for the trajectory. Interpolated from numSections sections
    void PlotCurve(){
        for (int i = 0 ; i < numSections; i++) {
            float t = (float)i / ((float)numSections - 1.0f);
            lR.SetPosition(i, GetQuadraticCoordinates(t));
        }//end for
    }//end PlotCurve

    //get the coordinates of the next point in the curve
    Vector3 GetQuadraticCoordinates(float t){
        return Mathf.Pow(1-t,2)*startPoint + 2*t*(1-t)*midPoint + Mathf.Pow(t,2)*endPoint;
    }//end GetQuadraticCoordinates
    /*********************/
}
