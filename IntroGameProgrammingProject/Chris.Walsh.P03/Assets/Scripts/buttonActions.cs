using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class buttonActions : MonoBehaviour {
    /*
     * Author: Chris Walsh
     * Created On: 12/11/18
     * Updated On: 12/11/18
     * Modified By: Chris Walsh
     */

    [Header("Set in Inspector")]
    public Button startButton = null;       //reference to start button on start screen
    public Button instructionButton = null; //reference to instructions button on start screen
    public Button quitButton = null;        //reference to quit button on start screen
    public Button loadTitle = null;         //reference to back button on instructions screen
 
    // Use this for initialization
    void Start () {
        if(startButton!=null)
            startButton.onClick.AddListener(StartGame);
        if (instructionButton != null)
            instructionButton.onClick.AddListener(LoadInstructions);
        if (quitButton != null)
            quitButton.onClick.AddListener(QuitGame);
        if (loadTitle != null)
            loadTitle.onClick.AddListener(LoadTitle);
    }//end Start
	
	// Update is called once per frame
	void Update () {
		
	}//end Update

    //call when start game button clicked
    void StartGame(){
        UnityEngine.SceneManagement.SceneManager.LoadScene(1);
        playerStats.PlayerLives = 3;
        playerStats.PlayerPoints = 0;
    }//end StartGame

    //call when instructions button clicked
    void LoadInstructions(){
        UnityEngine.SceneManagement.SceneManager.LoadScene(2);
    }//end LoadInstructions

    //call to quit game
    void QuitGame(){
        Application.Quit();
    }//end QuitGame

    //call to return to title from instructions
    void LoadTitle(){
        UnityEngine.SceneManagement.SceneManager.LoadScene(0);
    }//end LoadTitle
}
