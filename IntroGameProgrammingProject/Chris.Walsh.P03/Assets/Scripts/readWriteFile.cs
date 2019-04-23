using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEditor;
using System.IO;
using UnityEngine.UI;
using System;

public class readWriteFile : MonoBehaviour {
    /*
     * Author: Chris Walsh
     * Created On: 12/11/18
     * Updated On: 12/13/18
     * Modified By: Chris Walsh
     */

    [Header("Set in Inspector")]
    public Text scoreText;          //High Scores list
    public Text highScoreIndicator; //high score indicator

    // Use this for initialization
    void Start () {
        highScoreIndicator.gameObject.SetActive(false);
        WriteString();
        ReadString();
	}//end Start

    /*********** Code From: https://support.unity3d.com/hc/en-us/articles/115000341143-How-do-I-read-and-write-data-from-a-text-file- ***********/
    //write the high score to the file
    void WriteString()
    {
        string path = "Assets/Resources/scores.txt";

        //Write some text to the scores.txt file
        StreamWriter writer = new StreamWriter(path, true);
        int value = playerStats.PlayerPoints;
        writer.WriteLine(value);
        writer.Close();
    }

    //read the score file and determine if the player got a high score
    void ReadString()
    {
        string path = "Assets/Resources/scores.txt";

        //Read the text from directly from the scores.txt file
        StreamReader reader = new StreamReader(path);
        string thisLine = reader.ReadLine();
        List<Int32> linesList = new List<Int32>();
        while(thisLine != null){
            linesList.Add(Int32.Parse(thisLine));
            thisLine = reader.ReadLine();
        }

        linesList.Sort();

        int count = 0;
        for(int i = linesList.Count-1; count < 10; i--){
            string line = linesList[i].ToString();
            scoreText.text += line +"\n";
            count++;
        }

        int topScore = linesList[linesList.Count - 1];
        if (topScore == playerStats.PlayerPoints){
            highScoreIndicator.gameObject.SetActive(true);
        }
        reader.Close();
    }
    /**************************************************************************/
}
