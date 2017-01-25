## Follow this guide to test the ML

### Two main sections of the ML system:
1. Danger rating from scale 1 - 10 given a latitude and longitude
2. Rate whether a report from a citizen is **IMPORTANT** or **NOT IMPORTANT**

### Getting started
#### Getting the danger rating system to work:
1. Clone the project
2. `cd GoDetroit`
3. `cd budi_ml`
4. `cd danger_rating`
5. `python model.py`
6. `python main.py`
7. Have fun playing with danger rating system!

#### Getting the feedback system to work:
1. Clone the project
2. `cd GoDetroit`
3. `cd budi_ml`
4. `cd feedback_nlp`
5. `cd gibberish_detector`
6. `python gib_detect_train.py`
7. `cd ..`
8. `python main.py "[SAMPLE REPORT HERE]"`
9. This will output the probability of a report being serious or not (from 0 to 1)
