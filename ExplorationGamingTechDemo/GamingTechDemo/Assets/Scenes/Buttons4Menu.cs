using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;

public class Buttons4Menu : MonoBehaviour
{
    public void loadSpace()
    {
        SceneManager.LoadScene("Space_Scene");
    }

    public void loadEqualizer()
    {
        SceneManager.LoadScene("The_Console");
    }

    public void loadCredits()
    {
        SceneManager.LoadScene("Credits");
    }

    private void Update()
    {
        if (Input.GetKeyDown ( KeyCode.Escape))
        {
            Application.Quit();
        }
    }
}
