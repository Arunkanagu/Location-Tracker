package dev.nura.locationtracker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dev.nura.locationtracker.R
import dev.nura.locationtracker.Utils.isValidEmail
import dev.nura.locationtracker.databinding.FragmentSignUpBinding
import dev.nura.locationtracker.realm.UserInfo
import dev.nura.locationtracker.viewmodal.AppViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AppViewModel by activityViewModels()

    private var userList: List<UserInfo> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.userLiveData.observe(requireActivity()) { list ->
            userList = listOf()
            userList = list
        }

        with(binding) {
            userText.setOnFocusChangeListener { _, _ ->
                binding.filledTextField1.error = ""
            }
            emailText.setOnFocusChangeListener { _, _ ->
                binding.filledTextField2.error = ""
            }
            passText.setOnFocusChangeListener { _, _ ->
                binding.filledTextField3.error = ""
            }
            confirmPassText.setOnFocusChangeListener { _, _ ->
                binding.filledTextField4.error = ""
            }
            signupBtn.setOnClickListener {
                dataVerification()
            }
            gotoSignIn.setOnClickListener {
                findNavController().popBackStack(R.id.loginFragment, false)
            }
        }
    }

    private fun dataVerification() {
        var allSet = true
        with(binding) {
            val name = userText.text.toString()
            val email = emailText.text.toString()
            val pass = passText.text.toString()
            val confirmPass = confirmPassText.text.toString()
            if (name.isEmpty()) {
                binding.filledTextField1.error = "Enter your name."
                allSet = false
            }
            if (!email.isValidEmail()) {
                binding.filledTextField2.error = "Enter the valid Email ID."
                allSet = false
            }else if (userList.any { it.userEmail == email }){
                binding.filledTextField2.error = "This Email ID has already been used."
                allSet = false
            }
            if (pass.length !in 4..8) {
                binding.filledTextField3.error = "Enter the valid Password. (must in 4 to 8 characters)."
                allSet = false
            }
            if (confirmPass.length !in 4..8) {
                binding.filledTextField4.error = "Enter the valid Password. (must in 4 to 8 characters)."
                allSet = false
            }
            if (confirmPass != pass) {
                binding.filledTextField4.error = "Password not matching, Try again."
                allSet = false
            }

            if (allSet) {
                viewModel.insertUser(UserInfo().apply {
                    userEmail = email
                    userName = name
                    password = pass
                })
                findNavController().popBackStack(R.id.loginFragment, false)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG: String = "SignUpFragment"
    }

}