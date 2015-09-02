# install git
sudo apt-get install git -y

# install java
sudo apt-get install default-jre -y
sudo apt-get install default-jdk -y

# install ffmpeg
sudo add-apt-repository ppa:mc3man/trusty-media -y
sudo apt-get update -y
sudo apt-get dist-upgrade -y
sudo apt-get install ffmpeg -y

# clone repo
git clone https://github.com/FauDroids/BabyFace.git
