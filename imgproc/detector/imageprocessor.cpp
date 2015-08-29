#include "imageprocessor.h"

ImageProcessor::ImageProcessor()
{

}

cv::Mat ImageProcessor::crop(const cv::Mat &input, const cv::Rect &roi) const
{
    int centerX = roi.x + roi.width/2;

    int offsetX = std::min(input.cols - centerX, centerX);
    int newWidth = 2 * offsetX;

    cv::Rect newRoi(centerX - offsetX, 0, newWidth, input.rows);
    cv::Mat newImage = input(newRoi);
    cv::Mat borderedImage(1080, 1920, newImage.type());

    if(newImage.cols < 1920) {
        int border = (1920 - newImage.cols)/2;
        newImage.copyTo(borderedImage(cv::Rect(border, 0, newImage.cols, newImage.rows)));
        return borderedImage;
    }

    return newImage;
}

cv::Mat ImageProcessor::scale(const cv::Mat &input) const
{
    cv::Mat scaledImage;

    double factor = static_cast<double>(input.cols)/input.rows;
    double newWidth = 1080 * factor;

    cv::resize(input, scaledImage, cv::Size(newWidth, 1080));

    return scaledImage;
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

