
function deleteClicked(gamePlayerId) {
    if (confirm('Are you sure you want to delete?')) {
    	window.location.href='/toc/mobile/gameplayer/delete/' + gamePlayerId;
    }
    return false;
}
