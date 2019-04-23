using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public static class playerStats{
    /*
     * Author: Chris Walsh
     * Created On: 12/11/18
     * Updated On: 12/12/18
     * Modified By: Chris Walsh
     */

    private static int playerPoints = 0;
    private static int playerLives = 3;

    //get and set points the player currently has
    public static int PlayerPoints
    {
        get
        {
            return playerPoints;
        }
        set
        {
            playerPoints = value;
        }
    }

    //get and set points the number of lives the player has
    public static int PlayerLives
    {
        get
        {
            return playerLives;
        }
        set
        {
            playerLives = value;
        }
    }
}