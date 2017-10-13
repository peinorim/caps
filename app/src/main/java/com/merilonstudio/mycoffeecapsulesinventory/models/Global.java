package com.merilonstudio.mycoffeecapsulesinventory.models;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.Date;

public class Global {

    public static Date adDate;
    public static String backupVal = "";
    public static DatabaseReference mDatabase;
    public static FirebaseAuth mAuth;
}
