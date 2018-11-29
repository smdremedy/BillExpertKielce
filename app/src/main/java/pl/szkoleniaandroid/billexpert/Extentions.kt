package pl.szkoleniaandroid.billexpert

import androidx.appcompat.app.AppCompatActivity

fun AppCompatActivity.getBillApi() = (this.application as App).billApi
