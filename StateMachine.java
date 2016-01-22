package com.example.admin.RoboDroid;

import android.util.Log;

/**
 * Created by victor on 08/01/2016.
 */
/*
Correspondance from binary to sensor
000 -> all ir sensor off
001 -> ir back sensor
010 -> ir left sensor
100 -> ir right sensor
110 -> ir right and left sensor
111 -> all ir sensor
 */
public class StateMachine {

    private enum StateRobot{FORWARD,RIGHT,LEFT,STOP,BACKWARDS};
    private static StateRobot robodroid;

    public StateMachine(){
        this.robodroid = StateRobot.FORWARD;
    }

    //Evolving
    public String Evolve(String strSensor, String strUltrasonicSensor){

        int iUltrasonicSensor=Integer.parseInt(strUltrasonicSensor);
        String strOrder=new String("");

        switch (this.robodroid){
            case STOP:
                if((strSensor.equals("000"))&&(iUltrasonicSensor>40)){
                    this.robodroid = StateRobot.FORWARD;
                }
                else if(strSensor.equals("010")){
                    this.robodroid = StateRobot.RIGHT;
                }
                else if(strSensor.equals("100")){
                    this.robodroid = StateRobot.LEFT;
                }
                else if(strSensor.equals("001")){
                    this.robodroid = StateRobot.FORWARD;
                }
                else if((iUltrasonicSensor <= 40)||(strSensor.equals("110"))){
                    this.robodroid = StateRobot.BACKWARDS;
                }
                break;
            case FORWARD:
                //goes right if too close to the wall
                if((iUltrasonicSensor <= 40)&&(strSensor.equals("000"))){
                    this.robodroid = StateRobot.RIGHT;
                }
                else if(strSensor.equals("111")){
                    this.robodroid = StateRobot.STOP;
                }
                else if((iUltrasonicSensor <= 40)&&(strSensor.equals("110"))){
                    this.robodroid = StateRobot.BACKWARDS;
                }
                else if(strSensor.equals("100")){
                    this.robodroid = StateRobot.LEFT;
                }
                else if(strSensor.equals("010")){
                    this.robodroid = StateRobot.RIGHT;
                }
                break;

            case BACKWARDS:
                // if too close to front and back wall
                if((strSensor.equals("001"))&&((iUltrasonicSensor <= 40))){
                    // and if left IR sensor activated or
                    this.robodroid = StateRobot.LEFT;
                }
                else if(strSensor.equals("111")){
                    this.robodroid = StateRobot.STOP;
                }
                else if(strSensor.equals("001")){
                    this.robodroid = StateRobot.FORWARD;
                }
                else if(iUltrasonicSensor > 55){
                    this.robodroid = StateRobot.LEFT;
                }
                break;

            case RIGHT:
                if(strSensor.equals("000")){
                    this.robodroid = StateRobot.FORWARD;
                }
                else if(strSensor.equals("111")){
                    this.robodroid = StateRobot.STOP;
                }
                else if(strSensor.equals("100")){
                    this.robodroid = StateRobot.LEFT;
                }
                break;

            case LEFT:
                if(strSensor.equals("000")){
                    this.robodroid = StateRobot.FORWARD;
                }
                else if(strSensor.equals("111")){
                    this.robodroid = StateRobot.STOP;
                }
                else if(strSensor.equals("001")){
                    this.robodroid = StateRobot.FORWARD;
                }
                else if(strSensor.equals("010")){
                    this.robodroid = StateRobot.RIGHT;
                }
                break;

        }
        strOrder=Action();
        return strOrder;

    }

    //Action
    public String Action(){
        //to send order
        String strOrder = new String("");

        switch (this.robodroid){
            case STOP:
                strOrder="S";
                break;
            case FORWARD:
                strOrder="H";
                break;

            case BACKWARDS:
                strOrder="B";
                break;

            case RIGHT:
                strOrder="D";
                break;

            case LEFT:
                strOrder="G";
                break;
        }
        return strOrder;
    }
}
