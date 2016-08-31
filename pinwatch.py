#!/usr/bin/python

import os
from tkinter import *

import threading

class Gpio:
    def __init__(self, file, number):
        self.file = file
        self.number = number

    def isOn(self):
        f = open(self.file,'r')
        contents = f.read().strip()
        return contents == "1"

base_dir = "mock/sys/class/gpio/"

class App:
    def __init__(self, master):
        self.frame = Frame(master)
        self.frame.pack()
        gpios = []
        self.gpios = gpios
        files = os.listdir(base_dir)
        for file in files:
          if file.find("gpio") ==0:
              path = base_dir+file+"/value"
              if os.path.exists(path):
                  gpios.append(Gpio(path,int(file[4:])))
        gpios = sorted(gpios,key=lambda gpio: gpio.number)

        for gpio in gpios:
            bgColor = "white"
            if gpio.isOn():
                bgColor = "orange"
            gpio.button = Button(
                self.frame, text=str(gpio.number) , bg=bgColor
            )
            gpio.button.pack(side=LEFT)


    def updateButtons(self):
        for gpio in self.gpios:
            bgColor = "white"
            if gpio.isOn():
                bgColor = "yellow"
            gpio.button.config(bg=bgColor)






root = Tk()

app = App(root)

def watch():
    app.updateButtons()
    t = threading.Timer(0.1, watch)
    t.start()
watch()
root.mainloop()
