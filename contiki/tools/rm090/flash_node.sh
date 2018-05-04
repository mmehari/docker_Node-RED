NODE_IDs=( 1 2 3 4 )
USB_PORTs=( "/dev/ttyUSB0" "/dev/ttyUSB1" "/dev/ttyUSB2" "/dev/ttyUSB3" )
contiki_root="/home/mmehari/Dropbox/workspace/BEOF/config/contiki"
APP="$contiki_root/examples/rime/single-hop_WSN.rm090"

for (( i = 0 ; i < ${#NODE_IDs[@]} ; i++ )) do
	tos-set-symbols --objcopy msp430-objcopy --objdump msp430-objdump --target ihex $APP test${NODE_IDs[$i]}.ihex node_id=${NODE_IDs[$i]} &
	$contiki_root/tools/rm090/ibcn-f5x-tos-bsl -c ${USB_PORTs[$i]} -5 -R --invert-reset --swap-reset-test -r -e -I -p test${NODE_IDs[$i]}.ihex &
done
rm test*
