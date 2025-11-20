const Footer = () => {
    const currentYear = new Date().getFullYear();

    return (
        <footer className="bg-gray-800 mt-16">
            <div className="container mx-auto py-6 px-4 sm:px-6 lg:px-8 text-center text-gray-400 text-sm">
                <p>&copy; {currentYear} SportsHub. All rights reserved.</p>
            </div>
        </footer>
    );
};

export default Footer;