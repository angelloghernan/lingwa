When a user saves a word, the word is saved in a table with pointers to the user and the saved word that shows how familiar the user is with a given word (the familiarity value starts at 0 if the word was saved by a user).

When a given user tries to practice, each of their words is fetched (including words chosen for them by the algorithm). The list of words is sorted by priority (a score based on if the user or the algorithm saved the word, how long the word is, and how familiar the user is with the given word).
If there are not enough words available for the user, the algorithm will fetch words from the user’s most recently read article (if available). First it will look in the word table to see if anyone else has saved enough words from the same article; if not, it will fetch the article’s content and add the longest words automatically to the user’s practice session. Only words with the highest relative priority are shown, with any not in the top 15 being left behind.

In calculating the priority, the algorithm first looks at if the word was saved during this session by the algorithm (not saved in the database yet). These words are given the lowest priority and only have a priority score equivalent to their word length.

If the word was saved previously by the algorithm, or saved by a user, the priority is calculated first by equaling the priority to the word’s length as a baseline. Then, the score is adds 5 * (max familiarity - familiarity). Then, this score is multiplied by two if the word was saved by a user (since presumably words saved by a user would tend to be more difficult or complex). Finally, the score subtracts (2 * streak count * familiarity score) if the user has a “winning streak” with the word, and adds (streak count * (max familiarity - familiarity)) if the user has a “losing streak” with the word. 

This works out so that words that the user is somewhat familiar with tend to show up in the middle or end of the pack so that their recall of the word’s meaning is tested better, and words that the user has gotten wrong multiple times in a row shows up first so they can learn the word more quickly. 

If a user has a very high winning streak with a word, it will likely not even show up in the quiz, and if they have a high losing streak, it will likely show up first every time until they have better mastered the word.

Currently, every time the user gets a given word correctly, their familiarity score goes up by 1. If they get the word incorrect or flip the flashcard for the word, their familiarity goes down by 1. The max familiarity score is 5, and the lowest is 0. As one would expect, their streak goes up by 1 for every consecutive win/loss.

If a user is struggling with a word a lot (meaning they get the word wrong more times than they get it right), the user will be prompted with a “quiz” showing the word’s meaning, before returning to the flashcard again, making them input the word 3 times in a row total in order to jog their memory or better teach them a word.

Once the user is finished practicing (either by finishing the quiz or hitting the back button), each word the user saw is updated/saved in the server, including words fetched by the algorithm. If the word was fetched by the algorithm, the table will note that the word was saved by the algorithm (while words that were manually added by a user will already be noted down as being saved by a user).

Possible improvements (for stretch goals, in order of difficulty):

- If the word is an algorithmically-picked word, the user is prompted with the “quiz” showing a definition automatically
- Familiarity declines over time, and streaks reset after a certain amount of time (a week?)
- Each word gets assigned a difficulty based on how often users tend to get the word wrong/right (especially on their first few tries)
- Users get a score for their fluency, which affects how influential their actions are on the difficulty of a word (getting a word wrong will affect the word more if they have a greater fluency score)
