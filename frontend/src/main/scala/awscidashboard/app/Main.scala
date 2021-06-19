package com.awscidashboard.app

import org.scalajs.dom
import org.scalajs.dom.document
import scala.scalajs.js.annotation.JSExportTopLevel

object Main:
  def main(args: Array[String]): Unit =
    println("halko")
    document.addEventListener(
      "DOMContentLoaded",
      { (e: dom.Event) =>
        setupUI()
      }
    )

def setupUI(): Unit =
  val button = document.createElement("button")
  button.textContent = "Click me!"
  button.addEventListener(
    "click",
    { (e: dom.MouseEvent) =>
      addClickedMessage()
    }
  )
  document.body.appendChild(button)

  appendPar(document.body, "Hello World!!!")

def appendPar(targetNode: dom.Node, text: String): Unit =
  val parNode = document.createElement("p")
  parNode.textContent = text
  targetNode.appendChild(parNode)

def addClickedMessage(): Unit =
  appendPar(document.body, "You clicked the button!")
