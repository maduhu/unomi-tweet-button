window.twttr = (function (d, s, id) {
    var js, fjs = d.getElementsByTagName(s)[0],
        t = window.twttr || {};
    if (d.getElementById(id)) return;
    js = d.createElement(s);
    js.id = id;
    js.src = "https://platform.twitter.com/widgets.js";
    fjs.parentNode.insertBefore(js, fjs);

    t._e = [];
    t.ready = function (f) {
        t._e.push(f);
    };

    return t;
}(document, "script", "twitter-wjs"));

// Load the context server script asynchronously
window.cxs = (function (document, elementToCreate, id) {
    var js, fjs = document.getElementsByTagName(elementToCreate)[0], cxs = window.cxs || {};
    if (document.getElementById(id)) return;
    js = document.createElement(elementToCreate);
    js.id = id;
    js.src = "http://localhost:8181/context.js";
    js.type = "text/javascript";
    fjs.parentNode.insertBefore(js, fjs);

    cxs._e = [];
    cxs.ready = function (f) {
        cxs._e.push(f);
    };

    return cxs;
}(document, 'script', 'context'));

// Wait for the asynchronous resources to load
twttr.ready(function (twttr) {
    // Now bind our custom intent events
    //twttr.events.bind('click', clickEventToAnalytics);
    twttr.events.bind('tweet', function (event) {

        var defaultErrorCallback = function () {
            alert('There was an error making the request.');
        };

        function contextRequest(successCallback, errorCallback, payload) {
            var data = JSON.stringify(payload);
            var url = 'http://localhost:8181/context.json?sessionId=' + window.cxs.sessionId;
            var xhr = new XMLHttpRequest();
            var isGet = data.length < 100;
            if (isGet) {
                xhr.withCredentials = true;
                xhr.open("GET", url + "&payload=" + encodeURIComponent(data), true);
            } else if ("withCredentials" in xhr) {
                xhr.open("POST", url, true);
                xhr.withCredentials = true;
            } else if (typeof XDomainRequest != "undefined") {
                xhr = new XDomainRequest();
                xhr.open("POST", url);
            }
            xhr.onreadystatechange = function () {
                if (xhr.readyState != 4) {
                    return;
                }
                if (xhr.status == 200) {
                    var response = xhr.responseText ? JSON.parse(xhr.responseText) : undefined;
                    successCallback(response);
                } else {
                    console.log("contextserver: " + xhr.status + " ERROR: " + xhr.statusText);
                    if (errorCallback) {
                        errorCallback(xhr);
                    }
                }
            };
            xhr.setRequestHeader("Content-Type", "text/plain;charset=UTF-8"); // Use text/plain to avoid CORS preflight
            if (isGet) {
                xhr.send();
            } else {
                xhr.send(data);
            }
        }

        var scope = 'unomi-tweet-button-sample';
        var itemId = btoa(window.location.href);
        var source = {
            itemType: 'page',
            scope: scope,
            itemId: itemId,
            properties: {
                url: window.location.href
            }
        };
        var contextPayload = {
            source: source,
            events: [
                {
                    eventType: 'tweetEvent',
                    scope: scope,
                    source: source
                }
            ],
            requiredProfileProperties: [
                'tweetNb',
                'tweetedFrom'
            ]
        };

        contextRequest(function (response) {
            console.log("Profile sucessfully updated with tweetNB = " + response.profileProperties.tweetNb + " and tweetedFrom = " + response.profileProperties.tweetedFrom);
        }, defaultErrorCallback, contextPayload);
    });
    //twttr.events.bind('retweet', retweetIntentToAnalytics);
    //twttr.events.bind('favorite', favIntentToAnalytics);
    //twttr.events.bind('follow', followIntentToAnalytics);
});