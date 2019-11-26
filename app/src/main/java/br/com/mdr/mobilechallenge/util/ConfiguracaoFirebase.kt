package br.com.mdr.mobilechallenge.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ConfiguracaoFirebase {

    companion object {
        //retorna a referencia do database
        fun getFirebase(): DatabaseReference? {
            return FirebaseDatabase.getInstance().reference
        }

        //Retorna instancia do FirebaseStorage
        fun getFirebaseStorage(): StorageReference? {
            return FirebaseStorage.getInstance().getReference()
        }

        fun getFirebaseAuth(): FirebaseAuth? {
            return FirebaseAuth.getInstance()
        }
    }
}