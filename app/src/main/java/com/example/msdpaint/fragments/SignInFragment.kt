package com.example.msdpaint.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import com.example.msdpaint.KtorClient
import com.example.msdpaint.MainActivity
import com.example.msdpaint.MsdPaintApplication
import com.example.msdpaint.R
import com.example.msdpaint.databinding.FragmentSignInBinding
import com.example.msdpaint.viewmodels.StorageViewModel
import com.example.msdpaint.viewmodels.StorageViewModelFactory
import com.example.msdpaint.viewmodels.StudioViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class SignInFragment : Fragment() {

    private lateinit var client: KtorClient
    private val storageVM: StorageViewModel by viewModels {
        StorageViewModelFactory(
            (requireActivity().application as MsdPaintApplication).doodleRepository
        )
    }
    private val studioVM: StudioViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentSignInBinding.inflate(inflater)

        binding.composeView.setContent {

            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {

                LoginArea(
                    modifier = Modifier
                        .align(Alignment.Center)
                )

                BackButton(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }

        }

        return binding.root

    }

    @Composable
    fun LoginArea(modifier: Modifier) {

        Surface(
            modifier = modifier
                .padding(32.dp)
                .wrapContentSize(),
            color = MaterialTheme.colorScheme.background
        ) {

            var user by remember { mutableStateOf(MainActivity.GLOBAL_USER) }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                if (user == null) {
                    SignInStuff()
                } else {
                    SignOutStuff()
                }

            }

        }

    }

    @Composable
    fun SignInStuff() {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            var email by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                },
                label = {
                    Text("Email")
                }
            )

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                },
                label = {
                    Text("Password")
                },
                visualTransformation = PasswordVisualTransformation()
            )

            Row {

                Button(onClick = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        Firebase.auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(requireActivity()) { task ->
                                if (task.isSuccessful) {
                                    MainActivity.GLOBAL_USER = Firebase.auth.currentUser
                                    client = KtorClient(MainActivity.GLOBAL_USER)
                                    lifecycleScope.launch() {
                                        client.sendUser(MainActivity.GLOBAL_USER!!.email.toString())
                                    }
                                    findNavController(this@SignInFragment).navigate(R.id.action_signInFragment_to_homeFragment)
                                } else {
                                    //Toast.makeText(context, "Login failed, try again", Toast.LENGTH_LONG).show()
                                    Toast.makeText(
                                        context,
                                        "Server is down indefinitely... sorry!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    Log.e("log in error", "${task.exception}")
                                }
                            }
                    } else {
                        Toast.makeText(
                            context,
                            "Enter your email and password to log in",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }) {
                    Text("Log In")
                }

                Button(onClick = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        Firebase.auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(requireActivity()) { task ->
                                if (task.isSuccessful) {
                                    MainActivity.GLOBAL_USER = Firebase.auth.currentUser
                                    Toast.makeText(context, "SignUp successful!", Toast.LENGTH_LONG)
                                        .show()
                                } else {
                                    //Toast.makeText(context, "User creation failed, try again", Toast.LENGTH_LONG).show()
                                    Toast.makeText(
                                        context,
                                        "Server is down indefinitely... sorry!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    Log.e("Create user error", "${task.exception}")
                                }
                            }
                    } else {
                        Toast.makeText(
                            context,
                            "Enter an email and password to sign up",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }) {
                    Text("Sign Up")
                }

            } // Row()

        } // Column()

    } // SignInStuff()

    @Composable
    fun SignOutStuff() {

        Button(onClick = {
            Firebase.auth.signOut()
            MainActivity.GLOBAL_USER = null
            storageVM.deleteAll()
            studioVM.resetStudio()
            findNavController(this@SignInFragment).navigate(R.id.action_signInFragment_to_homeFragment)
        }) {
            Text("Sign out")
        }

        Button(onClick = {
            findNavController(this@SignInFragment).navigate(R.id.action_signInFragment_to_homeFragment)
        }) {
            Text("Go To Home")
        }

    }

    @Composable
    fun BackButton(modifier: Modifier) {

        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {

            Image(
                painter = painterResource(id = R.drawable.back_arrow),
                contentDescription = "sign in fragment back (to home) button",
                modifier = Modifier.clickable { findNavController(this@SignInFragment).navigate(R.id.action_signInFragment_to_homeFragment) }
            )

        }

    }

}