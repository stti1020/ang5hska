import { Pipe, PipeTransform } from '@angular/core';
import { RoutingEntry } from '../models/routingEntry';


@Pipe({
  name: 'routingEntryFilter'
})
export class RoutingEntryFilterPipe implements PipeTransform {

  transform(items: RoutingEntry[], filter: RoutingEntry): RoutingEntry[] {
    if (!items || !filter) {
      return items;
    }
     return items.filter((item: RoutingEntry) => this.applyFilter(item, filter));
  }

  applyFilter(routingEntry: RoutingEntry, filter: RoutingEntry): boolean {
    for (let field in filter) {
      if (filter[field]) {
        if (typeof filter[field] === 'string') {
          if (routingEntry[field].toLowerCase().indexOf(filter[field].toLowerCase()) === -1) {
            return false;
          }
        } else if (typeof filter[field] === 'number') {
          if (routingEntry[field] !== filter[field]) {
            return false;
          }
        }
      }
    }
    return true;
  }
}
