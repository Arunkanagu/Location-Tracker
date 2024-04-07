package com.example.locationtracker.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.locationtracker.R
import com.example.locationtracker.commen.AppPreferences
import com.example.locationtracker.databinding.FragmentHomeBinding
import com.example.locationtracker.realm.UserInfo
import com.example.locationtracker.viewmodal.AppViewModel
import java.util.Date


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AppViewModel by activityViewModels()

    private var userList: List<UserInfo> = listOf()

    private var logInList : List<UserInfo> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
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
            logInList = list.filter { it.loginState }
        }

    }

    private fun updateLogInData(id:String, logIn:Boolean = true ){
        if (logIn){
            AppPreferences.loginUuid = id
        }else{

        }

        val user = userList.find { (it._id.toString() == id) }
        if (user != null) {
            viewModel.updateCourse(UserInfo().apply {
                _id = user._id
                loginState = logIn
                userEmail = user.userEmail
                userName = user.userName
                password = user.password
                lastLoginDate = Date().time
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG: String = "HomeFragment"
    }

}