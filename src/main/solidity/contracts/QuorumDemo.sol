pragma solidity 0.5.0;

/**
 * @dev Smart-Contract for demonstration purposes.
 */
contract QuorumDemo {

    string public user;

    /**
     * @dev Rewrite user name in storage.
     */
    function writeUser(string memory _user) public {
        user = _user;
    }
}
