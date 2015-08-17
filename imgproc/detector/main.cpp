#include <iostream>
#include <string>

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>

#include <boost/program_options.hpp>

//Cut down characters
namespace po = boost::program_options;

enum RETURN_VALUES {
    SUCCESS = 0,
    COMMAND_LINE_ERROR = 1
};

int main(int argc, char *argv[])
{
    // Declare the supported options.
    po::options_description desc("Options");
    desc.add_options()
        ("help", "I'm here to help you")
        ("process", po::value<std::string>(), "set compression level")
    ;

    po::variables_map vm;

    try {
        po::store(po::parse_command_line(argc, argv, desc), vm);
        po::notify(vm);

        if (vm.count("help")) {
            std::cout << desc << std::endl;
            return 1;
        }

        if (vm.count("compression")) {
            std::cout << "Compression level was set to "
         << vm["compression"].as<int>() << "." << std::endl;
        } else {
            std::cout << "Compression level was not set." << std::endl;
        }
    } catch (po::error &e) {
        std::cerr << "ERROR: " << e.what() << std::endl << std::endl;
        std::cout << desc << std::endl;
        return RETURN_VALUES::COMMAND_LINE_ERROR;
    }

    return RETURN_VALUES::SUCCESS;
}

