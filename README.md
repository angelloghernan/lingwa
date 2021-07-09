Original App Design Project - README Template
===

# Lingwa
(Name is tentative)

## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)
2. [Schema](#Schema)

## Overview
### Description
This app allows the user to import books and articles in other languages and receive instant translation through Google Translate as they read (clicking on individual words to see translations). Any words the user needs help with, they can save for later to memorize using flashcards. Users can also read book passages and articles uploaded by others and communicate with other users to practice further. 

### App Evaluation
- **Category:** Education / Social
- **Mobile:** Will exist only on mobile (as far as this project goes). Will work best on mobile devices as it will be used primarily to read and memorize -- otherwise, all features could exist on a desktop computer well.
- **Story:** Allows users to advance their reading skills in another language through content they choose, as well as share content and communicate with other learners.
- **Market:** Anyone who wants to learn another language could find this app handy. As it is, there are very few apps with this kind of functionality, and all are limited without paying for premium features.
- **Habit:** As revealed with services like Duolingo, language learning is a habit that can be induced very potently. Since users can choose what content they want to see (and possibly have content recommended to them), the opportunity is there for people to use their online browsing time to learn and have fun at the same time.
- **Scope:** Focused primarily on users' own content, and on other users' content second. Could potentially allow users to import social media feeds from elsewhere later on (ie: seeing topics trending on Twitter in a Spanish-speaking country)

## Product Spec

### 1. User Stories (Required and Optional)

**Required Must-have Stories**

* User can sign up, log in, and log out.
* User can import ebooks saved on phone (using epub format) to read and save on their account
* User can import news articles using a URL to read
* User can share imported articles as public content for others to view
* User can tap on words in imported books & articles to see a translation to English, if applicable
* User can save words to learn later on through flashcards
* User can search for another user and follow them
* User can see public content from followed users
* User can make regular posts without any content attached

**Optional Nice-to-have Stories**

* User can leave comments under other users' posts (almost required)
* User can message other users privately if they mutually follow each other (*almost* a required story)
* User can upload a profile picture and a short bio (also almost required)
* User can favorite/like other users' content and posts to save for later
* User can see trending topics in selected countries and read posts from people in those countries (at least from Twitter)
* User can translate trending posts just like imported articles and books


### 2. Screen Archetypes

* Sign up page
   * User can sign up
   * * Sign up page asks for an email+password and which language the user wants to learn
   * Alternatively, if user is already signed up, they can navigate to an alternative log in page to log in
* Home Screen
   * User can see content from followed users
   * * If user does not follow any users, user can see a prompt to follow users or go to My Profile to read own content
   * User can navigate to followed users profile pages by clicking on their profile picture or view content on their feed
   * User can navigate to My Profile page or search for users
* Search
   * User can search for other users
   * User can click on other users to see their profile (brings to user profile)
* User Profile
   * User can follow selected user
   * User can browse through the selected users' public content and posts and leave comments underneath
   * User can see the other users' details (username, profile picture, optional bio)
   * User can go to My Stuff if the profile is their profile
* Content View
   * User can read selected content
   * User can tap on individual words to see a translation
   * User can exit back to whichever screen they were on before viewing content
* My Stuff
   * User can see uploaded books
   * User can see bookmarked posts
   * User can see saved words and train using flashcards
* Flashcards
   * User can see flashcards with saved words on them
   * User can type translation for each word
   * User is notified if their translation is correct
### 3. Navigation

**Tab Navigation** (Tab to Screen)

* Home Screen
* Search
* My profile

**Flow Navigation** (Screen to Screen)
* Log in/Sign up screen
   * Home screen (After logging in/signing up)
* Home Screen
   * Content View (via clicking on followed user content)
   * User Profile (via clicking on followed user profile picture)
* Search
   * User Profile (via clicking on searched user profile)
* My Profile 
   * Content View (via clicking on own uploaded content)
* User Profile
    * Content View (via clicking on user uploaded content)
* Content View 
    * My profile, Home Screen, Search, User profile (via exiting content view, dependent on where content view was created)

## Wireframe/Basic Design
(click to see bigger picture)
<img src="https://raw.githubusercontent.com/angelloghernan/lingwa/main/Wireframe2.png" width=600>

## Schema 
### Models
User
(excluding default properties: objectId, password, username, email, etc)
| Property     |Type      |Description      |
|-------|-------|-------|
|profilePicture      |File      |user's profile picture |
|Bio|String|short bio written by user|
|followersCount|Number|number of followers for user|
|savedWords|Array|contains all of user's saved words|
|languagesLearning|Array|contains list of languages user is learning ("EN", "SP", "FR", etc)|

FollowTable
|Property|Type|Description|
|--------|----|-----------|
|follower|Pointer|points to User following the *followed* user|
|followed|Pointer|points to User being followed by the *follower* user|

Post
(excluding default properties: objectId, createdAt, updatedAt)
|Property|Type|Description|
|--------|----|-----------|
|author|Pointer|points to User who posted|
|body|String|contains post content|
|scope|String|contains privacy setting (available to *"all"*, available to *"friends"*, available to *"self"* only)|
|likesCount|Number|number of likes on post|
|replyCount|Number|number of replies to post|

Reply
(excluding default proprties: objectId, createdAt, updatedAt)
|Property|Type|Description|
|--------|----|-----------|
|author|Pointer|points to User who posted|
|replyingTo|Pointer|points to Post reply is under|
|body|String|contains reply content|

LikeTable
|Property|Type|Description|
|--------|----|-----------|
|post|Pointer|points to Post being liked|
|likedBy|Pointer|points to User liking post|

Content
(excluding default properties: objectId, createdAt, updatedAt)
|Property|Type|Description|
|--------|----|-----------|
|uploader|Pointer|points to User who uploaded the book (if article, point to admin user)|
|author|String|(nullable) contains book or article author if available|
|type|String|either a "book" or "article"|
|cover|File|(nullable) contains book cover or article image if available|
|document|File|(nullable) contains epub file if a book|
|articleContent|String|contains article content if an article|

(Stretch goal model)
Message
|Property|Type|Description|
|--------|----|-----------|
|sender|Pointer|points to User who sent message|
|receiver|Pointer|points to User who received message|
|body|String|contains message content|
|createdAt|Date|(default) time when message was sent, used for reconstructing conversations|




### Networking
* Sign up page
   * (POST/Create): Sign up user using username and password
* Home Screen
   * (READ/Get): Get posts from followed users and from current user, listed by most recent
   ```java
    new Thread(new Runnable() { @Override public void run() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        ParseQuery<FollowTable> followingQuery = ParseQuery.getQuery("FollowTable");
        
        // Get all users that the current user is following, 
        // add current user to list
        followingQuery.whereEqualTo("follower", currentUser.getObjectId());
        List<User> users = followingQuery.find();
        
        // make sure the query succeeded before proceeding
        if (users != null) {
            users.add(currentUser);
        
            // Get last 20 posts created by self or followed users, ordered by
            // most recent
            ParseQuery<Post> postQuery = ParseQuery.getQuery("Post");
            postQuery.include("author");
            postQuery.whereContainedIn("author", users);
            postQuery.setLimit(20);
            postQuery.addDescendingOrder("createdAt");

            posts.addAll(postQuery.find());
            // Finally, notify the adapter that the posts
            // are now available
            adapter.notifyDatasetChanged();
        } else {
            // error handling goes here
        }
    } } ).start();
  
   ```
   * (POST/Create): If current user creates new post, create a new Post
   * (POST/Update): If current user likes post, tell server
   * (POST/Create): If current user replies to a post, create a Reply
* Search
   * (READ/Get): Get users with matching keywords in their name and posts with matching keywords in their name (may or may not be feasible)
* User Profile
   * (READ/Get): Get user information (username, bio, profilePicture)
   * (READ/Get): Get posts created by user and show under main profile
   * (POST/Update): If current user likes/unlikes post, tell server
   * (POST/Create): If current user replies to a post, create a Reply
   * (POST/Create): If current user follows user, create new entry in follower table
   * (Delete): If current user unfollows user, delete entry in follower table
* My Profile
   * (READ/Get): Get same user information as above
   * (READ/Get): Get books uploaded by user, liked posts, and saved words for display in My Stuff page
   * (POST/Create): If user uploads book, create new Book
* Content View
   * (READ/Get): Get book (epub) information to be displayed
   * (READ/Get): Alternatively, get article information to be displayed
   * (POST/Update): If user saves a word, update savedWord


original at https://hackmd.io/xSUJQve6Q0yqeiNziqIqQQ
