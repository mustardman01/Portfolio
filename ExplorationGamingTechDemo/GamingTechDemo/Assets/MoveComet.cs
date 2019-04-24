using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class MoveComet : MonoBehaviour
{
    public float speed = 20;
    public float xRadius = 20;
    public float xOffset = 0;
    public float yOffset = 0;
    public float yRadius = 0;
    public float zRadius = 20;
    public float zOffset = 0;
    public bool useVariableSpeed = false;

    private float timeCounter = 0;
    private float x = 1;
    private float y = 0;
    private float z = 0;
    
    void Start()
    {
        transform.position = new Vector3(x, y, z);
        yRadius = Random.Range(-10, 10);
    }

    // Update is called once per frame
    void Update()
    {
        if (useVariableSpeed){
            if (x < 5)
                x = 5;
            timeCounter += (1 / x) * speed * Time.deltaTime;
        }
        else{
            timeCounter += speed * Time.deltaTime;
        }
        
        x = Mathf.Cos(timeCounter)*xRadius + xOffset;
        z = Mathf.Sin(timeCounter)*zRadius + zOffset;
        y = Mathf.Cos(timeCounter) * yRadius + yOffset;
        transform.position = new Vector3(x, y, z);
    }
}
