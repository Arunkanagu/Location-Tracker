package com.example.locationtracker.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.locationtracker.R
import com.example.locationtracker.Utils.isValidEmail
import com.example.locationtracker.commen.AppPreferences
import com.example.locationtracker.databinding.FragmentLoginBinding
import com.example.locationtracker.realm.UserInfo
import com.example.locationtracker.viewmodal.AppViewModel
import java.util.Date


class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AppViewModel by activityViewModels()

    private var userList: List<UserInfo> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            })

        viewModel.userLiveData.observe(requireActivity()) { list ->
            userList = listOf()
            userList = list
            if (userList.isEmpty()) {
                findNavController().navigate(R.id.signUpFragment)
            }
        }


        with(binding) {
            emailText.setOnFocusChangeListener { _, _ ->
                filledTextField1.error = ""
            }
            passText.setOnFocusChangeListener { _, _ ->
                filledTextField2.error = ""
            }
            loginBtn.setOnClickListener {
                logInVerification()
            }
            gotoSignUp.setOnClickListener { findNavController().navigate(R.id.signUpFragment) }
        }
    }

    private fun logInVerification() {
        var allSet = true
        with(binding) {
            val email = emailText.text.toString()
            val pass = passText.text.toString()
            if (!email.isValidEmail()) {
                binding.filledTextField1.error = "Enter the valid Email ID"
                allSet = false
            }
            if (pass.length !in 4..8) {
                binding.filledTextField2.error =
                    "Enter the valid Password. (must in 4 to 8 characters)"
                allSet = false
            }
            if (allSet) {
                val user = userList.find { it.userEmail == email }
                if (user != null) {
                    if (user.password == pass) {
                        AppPreferences.loginUuid = user._id.toString()
                        user.loginState = true
                        user.lastLoginDate = Date().time
                        viewModel.updateCourse(UserInfo().apply {
                            _id = user._id
                            loginState = true
                            userEmail = user.userEmail
                            userName = user.userName
                            password = user.password
                            lastLoginDate = Date().time
                        })
                        findNavController().navigate(R.id.homeFragment)
                    } else {
                        Toast.makeText(
                            requireContext(), "Invalid Email-Id and Password.", Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(), "Invalid Email-Id and Password.", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG: String = "LoginFragment"
    }

}