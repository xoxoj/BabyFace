#include "imageprocessor.h"

ImageProcessor::ImageProcessor()
{

}

cv::Mat ImageProcessor::crop(cv::Mat &input, const cv::Rect &roi) const
{
    int centerX = roi.x + roi.width/2;
    int centerY = roi.y + roi.height/2;

    int newWidth = roi.width * 1.15;
    int newHeight = roi.height * 1.15;

    int offsetX = newWidth/2;
    int offsetY = newHeight/2;

    if(centerX + offsetX >= input.cols || centerX - offsetX < 0) {
        offsetX = std::min(input.cols - centerX, centerX);
        newWidth = 2 * offsetX;
    }
    if(centerY + offsetY >= input.rows || centerY - offsetY < 0) {
        offsetY = std::min(input.rows - centerY, centerY);
        newHeight = 2 * offsetY;
    }

    cv::Rect newRoi(centerX - offsetX, centerY - offsetY, newWidth, newHeight);
    cv::Mat newImage = input(newRoi);

    return newImage;
}

cv::Mat ImageProcessor::equalizeHistogram(const cv::Mat &input) const
{
    cv::Mat ycrcb;

#if CV_MAJOR_VERSION >= 3
    cv::cvtColor(input, ycrcb, cv::COLOR_BGR2YCrCb);
#else
    cv::cvtColor(input, ycrcb, CV_BGR2YCrCb);
#endif

    std::vector<cv::Mat> channels;
    cv::split(ycrcb, channels);

    cv::equalizeHist(channels[0], channels[0]);

    cv::Mat result;
    cv::merge(channels, ycrcb);

#if CV_MAJOR_VERSION >= 3
    cv::cvtColor(ycrcb, result, cv::COLOR_YCrCb2BGR);
#else
    cv::cvtColor(ycrcb, result, CV_YCrCb2BGR);
#endif

    return result;
}

bool ImageProcessor::applyMorphing() const
{
    return this->morphing;
}

void ImageProcessor::applyMorphing(const bool apply)
{
    this->morphing = apply;
}

bool ImageProcessor::enhance() const
{
    return this->enhancement;
}

void ImageProcessor::enhance(const bool apply)
{
    this->enhancement = apply;
}

