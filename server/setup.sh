# install git
sudo install git
sudo apt-get install git

# install java
sudo apt-get install default-jre
sudo apt-get install default-jdk

# install ffmpeg
sudo add-apt-repository ppa:mc3man/trusty-media
sudo apt-get update
sudo apt-get dist-upgrade
sudo apt-get install ffmpeg

# clone repo
git clone https://github.com/FauDroids/BabyFace.git
cd BabyFace/server

# run
./gradlew run
