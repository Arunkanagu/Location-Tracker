package dev.nura.locationtracker.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import dev.nura.locationtracker.MainActivity
import dev.nura.locationtracker.R
import dev.nura.locationtracker.adapter.AccountAdapter
import dev.nura.locationtracker.adapter.LocationAdapter
import dev.nura.locationtracker.commen.AppPreferences
import dev.nura.locationtracker.databinding.DailogUserSwitchBinding
import dev.nura.locationtracker.databinding.FragmentHomeBinding
import dev.nura.locationtracker.realm.LocationInfo
import dev.nura.locationtracker.realm.UserInfo
import dev.nura.locationtracker.viewmodal.AppViewModel
import java.util.Date


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AppViewModel by activityViewModels()

    private var userList: List<UserInfo> = listOf()
    private var locationList: List<LocationInfo> = listOf()

    private var logInList: List<UserInfo> = listOf()

    private var currentUser = MutableLiveData<UserInfo?>()

    private lateinit var alertDialog: AlertDialog

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



        (activity as MainActivity).checkLocationPermission()

        viewModel.haveAllPermissions.observe(viewLifecycleOwner){
            Log.i(TAG, "onViewCreated: haveAllPermissions = $it")
            if (it){
                binding.havePermission.visibility = View.VISIBLE
                binding.notPermission.visibility = View.GONE
            }else{
                binding.notPermission.visibility = View.VISIBLE
                binding.havePermission.visibility = View.GONE
            }
        }

        viewModel.userLiveData.observe(viewLifecycleOwner) { list ->
            userList = listOf()
            userList = list
            userDataUpdate()

        }
        viewModel.locations.observe(viewLifecycleOwner) { loca ->
            updateUserLocationData(loca)
        }

        currentUser.observe(viewLifecycleOwner) {
            with(binding) {
                if (it != null) {
                    account.text = it.userName[0].toString().uppercase()
                    viewModel.localUserInfo = it
                }
            }
        }

        with(binding) {
            account.setOnClickListener {
                showDialog(logInList)
            }
        }


    }

    private fun updateUserLocationData(loca: List<LocationInfo>) {
        locationList = listOf()
        val id = AppPreferences.loginUuid.toString()
        locationList = loca.filter { it.userId.toString() == id }

        Log.w(TAG, "onViewCreated: locationList = ${locationList.size}")
        if (locationList.isEmpty()) {
            binding.locationList.visibility = View.GONE
            binding.empty.visibility = View.VISIBLE
        } else {
            binding.locationList.visibility = View.VISIBLE
            binding.empty.visibility = View.GONE
        }
        updateAdapter()
    }

    private fun userDataUpdate() {
        if (userList.isEmpty()) {
            findNavController().navigate(R.id.signUpFragment)
        }
        val c = userList.find { it._id.toString() == AppPreferences.loginUuid }

        currentUser.postValue(c)
        logInList =
            userList.filter { it.loginState && it._id.toString() != AppPreferences.loginUuid }

        (activity as MainActivity).checkLocationPermission()
        Log.w(TAG, "onViewCreated: userLiveData ${logInList.size}")
    }

    private fun updateAdapter() {
        val adapter = LocationAdapter(locationList, object : LocationAdapter.OnClickListener {
            override fun onClick(item: LocationInfo) {
                viewModel.localLocationInfo.postValue(item)
                findNavController().navigate(R.id.mapViewFragment)
            }
        })
        binding.locationList.adapter = adapter
    }

    private fun showDialog(userList: List<UserInfo>) {
        try {

            val builder = AlertDialog.Builder(requireContext())
            val bindingAlert = DailogUserSwitchBinding.inflate(layoutInflater)
            builder.setView(bindingAlert.root)
            builder.setCancelable(false)
            alertDialog = builder.create()
            with(bindingAlert) {
                val currentAcc =
                    currentUser.value
                close.setOnClickListener {
                    alertDialog.dismiss()
                }
                if (logInList.isEmpty()) {
                    loginListView.visibility = View.GONE
                } else {
                    loginListView.visibility = View.VISIBLE
                    logInList
                }
                val accountAdapter =
                    AccountAdapter(logInList, object : AccountAdapter.OnClickListener {
                        override fun onClick(item: UserInfo) {
                            AppPreferences.loginUuid = item._id.toString()
                            userDataUpdate()
                            viewModel.locations.value?.let { updateUserLocationData(it) }
                            alertDialog.dismiss()
                        }
                    })
                loginListView.adapter = accountAdapter
                if (currentAcc != null) {
                    name.text = currentAcc.userName
                    email.text = currentAcc.userEmail
                    account.text = currentAcc.userName[0].toString()
                } else {
                    name.text = "No data"
                    email.text = "No data"
                    account.text = "ND"
                }
                addUserAccount.setOnClickListener {
                    findNavController().navigate(R.id.loginFragment)
                }
            }
            alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            alertDialog.show()
        } catch (e: Exception) {
            Log.e(TAG, "showDialog: ${e.message.toString()}")
        }
    }

    private fun updateLogInData(id: String, logIn: Boolean = true) {
        AppPreferences.loginUuid = id
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

    override fun onPause() {
        super.onPause()
        if (this::alertDialog.isInitialized) {
            if (alertDialog.isShowing) {
                alertDialog.dismiss()
            }
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