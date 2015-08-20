#include "imageprocessor.h"

ImageProcessor::ImageProcessor()
{

}

cv::Mat ImageProcessor::crop(cv::Mat &input, const cv::Rect &roi) const
{
    //TODO Cropping code
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

void ImageProcessor::applyMorphing(bool apply)
{
    this->morphing = apply;
}

