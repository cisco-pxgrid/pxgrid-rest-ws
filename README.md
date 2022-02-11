# pxGrid
pxGrid is a protocol framework that defines the control mechanisms to facilitate machine-to-machine communications.

Benefits of using pxGrid:
- Reduce complexity of meshed network
- Centralized authentication and authorization
- Service abstraction
- Minimize human configuration errors

This project contains documentation and samples to use pxGrid.

See [documentation](https://github.com/cisco-pxgrid/pxgrid-rest-ws/wiki) to learn how to use pxGrid.

# Loss Detection
Loss Detection helps pxGrid clients to detect if they have missed any messages over the web-socket connection. 
Every message would contain a sequence number, which the clients can keep track of. 
If the clients miss a sequence number, then they can recover the data using the REST API for bulk download.

See [documentation](https://developer.cisco.com/docs/pxgrid/#!loss-detection/loss-detection) for more details

