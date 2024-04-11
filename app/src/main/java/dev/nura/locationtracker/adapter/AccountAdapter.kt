package dev.nura.locationtracker.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.nura.locationtracker.databinding.LayoutAccountItemBinding
import dev.nura.locationtracker.realm.UserInfo

class AccountAdapter(
    private var list: List<UserInfo>,
    private val listener: OnClickListener
) : RecyclerView.Adapter<AccountAdapter.AccountViewHolder>() {

    interface OnClickListener {
        fun onClick(item: UserInfo)
    }

    init {
        setHasStableIds(true)
    }
    class AccountViewHolder (val binding: LayoutAccountItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        return AccountViewHolder(
            LayoutAccountItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        holder.setIsRecyclable(false)
        val acc = list[position]
        with(holder.binding) {
            name.text = acc.userName
            email.text = acc.userEmail
            account.text= acc.userName[0].toString()

            constraintLayout.setOnClickListener {
                listener.onClick(acc)
            }
        }
    }
}