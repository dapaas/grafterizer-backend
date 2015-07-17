module.exports = function(Function) {
    // Function.disableRemoteMethod('deleteById', true);

    var methods = [
        'find',
        'create',
        'prototype_updateAttributes',
        'findById'];

    Function.sharedClass.methods().forEach(function(method) {
        method.shared = methods.indexOf(method.name) > -1;
    });

    Function.observe('before save', function(ctx, next) {
        if (ctx.isNewInstance) {
            ctx.instance.downloads = 0;
        }
        next();
    });

    Function.afterRemote('findById', function(ctx, affectedModelInstance, next) {
        if (affectedModelInstance) {
            affectedModelInstance.updateAttribute('downloads', affectedModelInstance.downloads+1||1);
        }
        next();
    });
};
