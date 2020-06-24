let ws = null;
let connectedUsers = [];
let messages = '';
let userName;

function startWs() {
    let chatbox = document.getElementById("chat");

    while(userName==null || userName=="") {
        userName = prompt("Username")
    }

    let roomId = prompt("Choose a room");

    ws = new WebSocket("ws://" + location.host + "/chat?userName=" + encodeURIComponent(userName) + "&roomId=" + encodeURIComponent(roomId));
    ws.onmessage = function(event) {

        console.log(event.data)

        // If message is json from backend
        if ((event.data.indexOf("[")) === 0) {
            connectedUsers = []
            var usersInfo = JSON.parse(event.data)
            for (var user of usersInfo){
                connectedUsers.push(user.name)
            }
            getUsers()
        }

        // If message is textmessage from user
        else {
            fillMessages(event.data)
            //chatbox.value = chatbox.value + "\n" + event.data;
            //chatbox.scrollTop = chatbox.scrollHeight;
        }
    }
}

function signout() {
    ws.close();
}

function sendMessage() {
    let input = document.getElementById("messageBox");
    ws.send(input.value);
    input.value = "";
}

function getUsers() {
    let userList='';
    for (var user of connectedUsers) {
        userList = userList + '<li class="connectedList">' +user+'</li>'
        console.log(userList)
    }
    document.getElementById("users").innerHTML = userList;
}

function fillMessages(message){
    let messageHeader;

    // Parse message json
    let jsonMessage = JSON.parse(message);

    // Extract content
    let content = jsonMessage.message;

    // Extract author name
    let author = jsonMessage.senderName;

    // Extract and convert timestamp to date
    let dateTime = timestampToDate(jsonMessage.timestamp);

    // Format message with header
    if(author === userName){
        messageHeader = '<li class="author">' + author + '<span class="date">' + dateTime + '</span> </li></div>';
        messages = messages + '<div class="left-message">' +  messageHeader + '<li class="own-message">' + content + '</li></div>' ;
    }

    else {
        messageHeader = '<li class="other-author">' + author + '<span class="date">' + dateTime + '</span> </li>';
        messages = messages + '<div class="right-message">' + messageHeader + '<li class="other-message">' + content + '</li></div>';
    }

    document.getElementById("messages").innerHTML = messages;
}

function timestampToDate(timestamp) {
    let date = new Date(timestamp).toISOString().slice(-13, -5);
    return date;
}