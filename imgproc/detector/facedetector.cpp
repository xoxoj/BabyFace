#include "facedetector.h"

#include <string>

FaceDetector::FaceDetector(const std::string &cascadeFile)
{
    this->classifier.load(cascadeFile);
}


cv::Rect FaceDetector::detect(const cv::Mat &input)
{
    std::vector<cv::Rect> faces;

    this->classifier.detectMultiScale(input, faces, 1.1, 5, 0);

    //If multiple "faces" are detected we'll pick the biggest one
    //since we're assuming that the babys face should be the main object.
    return this->findLargestFace(faces);
}


cv::Rect FaceDetector::findLargestFace(const std::vector<cv::Rect> &faces) const
{
    cv::Rect largestFace;

    for(auto face : faces) {
        double aspectRatio = face.width / static_cast<double>(face.height);

        //Unless you are taking pictures of horses the aspect ratio
        //of a face shouldn't be too different from a square
        if(aspectRatio < 0.85) {
            continue;
        }
        if(face.width > largestFace.width &&
           face.height > largestFace.height) {
            largestFace = face;
        }
    }

    return largestFace;
}
